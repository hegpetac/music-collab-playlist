import {Component, computed, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {
  DashboardService,
  DashboardSettings,
  HandlePlaylistService, PlaybackState, PlaybackStateMessage, PlaybackStatus, Provider, TrackList,
  TrackSummary, YoutubePlaybackMode
} from '../../../openapi';
import {WebSocketService} from '../../services/websocket.service';
import {combineLatest} from 'rxjs';
import {CdkDragDrop, CdkDropList, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {TrackContainer} from './track-container/track-container';
import {Search} from './search/search';
import {MatSlider, MatSliderThumb} from '@angular/material/slider';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MsToTimePipe} from '../../pipes/ms-to-time-pipe';

@Component({
  selector: 'app-playlist-manager',
  imports: [
    CdkDropList,
    TrackContainer,
    Search,
    MatSlider,
    MatSliderThumb,
    MatIconButton,
    MatIcon,
    MsToTimePipe
  ],
  templateUrl: './playlist-manager.html',
  styleUrl: './playlist-manager.css',
})
export class PlaylistManager implements OnInit, OnDestroy {
  public suggestions: TrackSummary[] = [];
  public queue: TrackSummary[] = [];
  public recommendations: TrackSummary[] = [];
  public settings: DashboardSettings | null = null;
  public state: PlaybackState | null = null;
  public isSeeking: boolean = false;
  public seekPreviewsMs: number = 0;
  public currentPosMs = signal(0);
  public totalDurationMs = signal(0);
  public progressPercent = computed(() => {
    const total = this.totalDurationMs();
    return total !== 0 ? Math.min((this.currentPosMs() / total) * 100, 100) : 0;
  })

  private _ws: WebSocketService = inject(WebSocketService);
  private _dashboardService: DashboardService = inject(DashboardService);
  private _playlistHandlerService = inject(HandlePlaylistService);
  private intervalId: number | undefined;

  public ngOnInit() {
    combineLatest([
      this._dashboardService.getDashboardSettings(),
      this._ws.isConnected()
    ])
    .subscribe(([settings]) => {
      this.settings = settings;
      this._ws.subscribe<TrackSummary[]>(
        `/topic/suggestions/${this.settings?.name}`,
        (data: TrackSummary[]) => {
          this.suggestions = data;
        }
      )
      this._ws.subscribe<TrackSummary[]>(
        `/topic/queue/${this.settings?.name}`,
        (data: TrackSummary[]) => {
          this.queue = data;
        }
      )
      this._ws.subscribe<TrackSummary[]>(
        `/topic/recommendations/${this.settings?.name}`,
        (data: TrackSummary[]) => {
          this.recommendations = data;
        }
      )
      this._ws.subscribe<PlaybackStateMessage>(
        `/topic/playback/${this.settings?.name}`,
        (data: PlaybackStateMessage) => {
          this.state = data;
          this.handlePlaybackStateMessage();
        }
      )
    });
    this.state = {
      activeTrack: {
        provider: Provider.Spotify,
        providerId: "1",
        title: "Nem Origó",
        artist: "Ákos",
        album: "Origo??",
        durationMs: 306445,
        thumbnail: "https://i.ytimg.com/vi/nSOMOXcviZc/default.jpg"
      },
      status: PlaybackStatus.Playing,
      positionMS: 111449
    }

    this.updateProgressPercent(this.state);
  }

  private handlePlaybackStateMessage() {
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
        //todo handle iFrame
      } else {
        //TODO pause tiltása Youtube ebben az esetben
        window.open(`https://www.youtube.com/watch?v=${this.state.activeTrack.providerId}`, '_blank');
      }
    }
  }

  private handlePausePlayback() {

  }

  private handleEmptyQueue() {
    //TODO
  }

  public onQueueDrop(event: CdkDragDrop<TrackSummary[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      this._playlistHandlerService.reorderQueue(this.queue.map(track => {
        return {
          providerId: track.providerId,
          provider: track.provider}
      }));
    } else if (event.previousContainer.id === 'recommendations') {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
      this._playlistHandlerService.addTrackFromList({
        track: this.queue.at(event.currentIndex),
        order: this.queue.map(track => {
          return {
            providerId: track.providerId,
            provider: track.provider
          }
        }),
        list: TrackList.Recommendations
      }).subscribe()
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      )
      this._playlistHandlerService.addTrackFromList({
        track: this.queue.at(event.currentIndex),
        order: this.queue.map(track => {
          return {
            providerId: track.providerId,
            provider: track.provider
          }
        }),
        list: TrackList.Suggestions
      }).subscribe()
    }
  }

  public deleteTrackFromList(track: TrackSummary, trackList: TrackList): void {
    this._playlistHandlerService.deleteTrack({
      providerId: track.providerId,
      list: trackList
    }).subscribe();
  }

  public togglePlayPause(): void{

  }

  public skip(): void {

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
    //TODO service call
  }

  private percentToMs(percent: number): number {
    return Math.round((this.totalDurationMs() ?? 0) * percent / 100);
  }

  private stopProgress(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  protected readonly TrackList = TrackList;
  protected readonly PlaybackStatus = PlaybackStatus;

  ngOnDestroy() {
    this.stopProgress();
  }
}
