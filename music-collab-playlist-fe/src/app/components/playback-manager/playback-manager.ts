import {Component, computed, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {combineLatest} from 'rxjs';
import {
  DashboardService,
  DashboardSettings,
  HandlePlaybackService,
  PlaybackState, PlaybackStatus, Provider,
  TrackSummary, YoutubePlaybackMode
} from '../../../openapi';
import {WebSocketService} from '../../services/websocket.service';
import {DEFAULT_TOTAL_DURATION} from '../../constants/defaults';
import {YoutubeAudioService} from '../../services/youtube-audio.service';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {MatSlider, MatSliderThumb} from '@angular/material/slider';
import {MsToTimePipe} from '../../pipes/ms-to-time-pipe';

@Component({
  selector: 'app-playback-manager',
  imports: [
    MatIcon,
    MatIconButton,
    MatSlider,
    MatSliderThumb,
    MsToTimePipe
  ],
  templateUrl: './playback-manager.html',
  styleUrl: './playback-manager.css',
})
export class PlaybackManager implements OnInit, OnDestroy {
  public settings: DashboardSettings | null = null;
  public state: PlaybackState | null = null;
  public isSeeking: boolean = false;
  public seekPreviewsMs: number = 0;
  public currentPosMs = signal(0);
  public totalDurationMs = signal(DEFAULT_TOTAL_DURATION);
  public progressPercent = computed(() => {
    const total = this.totalDurationMs();
    return total !== 0 ? Math.min((this.currentPosMs() / total) * 100, 100) : 0;
  })

  private _ws: WebSocketService = inject(WebSocketService);
  private _dashboardService: DashboardService = inject(DashboardService);
  private _playbackHandlerService = inject(HandlePlaybackService);
  private intervalId: number | undefined;
  private _youtubeAudioService = inject(YoutubeAudioService);

  public ngOnInit() {
    combineLatest([
      this._dashboardService.getDashboardSettings(),
      this._ws.isConnected()
    ])
      .subscribe(([settings]) => {
        this.settings = settings;
        this._ws.subscribe<PlaybackState>(
          `/topic/playback/${this.settings?.name}`,
          (data: PlaybackState) => {
            console.log(data)
            this.state = data;
            this.handlePlaybackStateMessage();
          }
        )
      });
  }

  private handlePlaybackStateMessage() {
    this._youtubeAudioService.pause();
    this.currentPosMs.set(this.state?.positionMS || 0);
    this.state?.activeTrack ?
      this.totalDurationMs.set(this.state.activeTrack.durationMs) :
      this.totalDurationMs.set(DEFAULT_TOTAL_DURATION);
    switch (this.state!.status) {
      case PlaybackStatus.Playing:
        this.handleStartTrack();
        break;
      case PlaybackStatus.Paused:
        this.handlePausePlayback();
        break;
      case PlaybackStatus.QueueEmpty:
        this.handleEmptyQueue();
        break;
    }
  }

  private handleStartTrack() {
    if (this.state?.activeTrack?.provider === Provider.Youtube) {
      if (this.settings?.youtubePlaybackMode === YoutubePlaybackMode.BuiltIn) {
        this._youtubeAudioService.play(this.state.activeTrack.providerId, this.state.positionMS! / 1000);
      } else {
        //TODO pause tiltása Youtube ebben az esetben
        window.open(`https://www.youtube.com/watch?v=${this.state.activeTrack.providerId}`, '_blank');
      }
    }
    this.startProgress();
  }

  private handlePausePlayback() {
    this.stopProgress();
  }

  private handleEmptyQueue() {
    this.stopProgress();
  }

  public togglePlayPause(): void{
    if (this.state?.status === PlaybackStatus.Playing) {
      this._playbackHandlerService.pause().subscribe();
    } else {
      this._playbackHandlerService.resume().subscribe();
    }
  }

  public skip(): void {
    this._playbackHandlerService.skip().subscribe();
  }

  private updateProgressPercent(state: PlaybackState): void {
    this.currentPosMs.set(state.positionMS!);
    this.totalDurationMs.set(state.activeTrack!.durationMs!)

    this.startProgress();
  }

  private startProgress(): void {
    this.stopProgress();

    const stepMs = 1000;
    this.intervalId = setInterval(() => {
      this.currentPosMs.update(pos => {
        const nextPos= pos + stepMs;
        if (nextPos >= this.totalDurationMs()) {
          this.stopProgress();
          return this.totalDurationMs();
        }
        return nextPos;
      });
    }, stepMs);
  }

  public onSeekStart(event: Event): void {
    this.isSeeking = true;
    const input = event.target as HTMLInputElement;
    this.seekPreviewsMs = this.percentToMs(+input.value);
  }

  public onSeekMove(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.seekPreviewsMs = this.percentToMs(+input.value);
  }

  public onSeekEnd(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.currentPosMs.set(this.percentToMs(+input.value));
    this.isSeeking = false;
    this._playbackHandlerService.seek({
      positionMs: this.currentPosMs()
    }).subscribe();
  }

  private percentToMs(percent: number): number {
    return Math.round((this.totalDurationMs() ?? 0) * percent / 100);
  }

  private stopProgress(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  protected readonly PlaybackStatus = PlaybackStatus;

  ngOnDestroy() {
    this.stopProgress();
  }
}
