import {Component, inject, OnInit} from '@angular/core';
import {
  DashboardService,
  DashboardSettings,
  HandlePlaylistService, TrackList,
  TrackSummary
} from '../../../openapi';
import {WebSocketService} from '../../services/websocket.service';
import {combineLatest} from 'rxjs';
import {CdkDragDrop, CdkDropList, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {TrackContainer} from './track-container/track-container';
import {Search} from './search/search';
import {MatIcon} from '@angular/material/icon';
import {PlaybackManager} from '../playback-manager/playback-manager';
import {Router} from '@angular/router';
import {MatIconButton} from '@angular/material/button';

@Component({
  selector: 'app-playlist-manager',
  imports: [
    CdkDropList,
    TrackContainer,
    Search,
    MatIcon,
    PlaybackManager,
    MatIconButton
  ],
  templateUrl: './playlist-manager.html',
  styleUrl: './playlist-manager.css',
})
export class PlaylistManager implements OnInit {
  public suggestions: TrackSummary[] = [];
  public queue: TrackSummary[] = [];
  public recommendations: TrackSummary[] = [];
  public settings: DashboardSettings | null = null;

  private _ws: WebSocketService = inject(WebSocketService);
  private _dashboardService: DashboardService = inject(DashboardService);
  private _playlistHandlerService = inject(HandlePlaylistService);
  private _router = inject(Router);

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
    });
    // this.state = {
    //   activeTrack: {
    //     provider: Provider.Spotify,
    //     providerId: "1",
    //     title: "Nem Origó",
    //     artist: "Ákos",
    //     album: "Origo??",
    //     durationMs: 306445,
    //     thumbnail: "https://i.ytimg.com/vi/nSOMOXcviZc/default.jpg"
    //   },
    //   status: PlaybackStatus.Playing,
    //   positionMS: 111449
    // }

    // this.updateProgressPercent(this.state);
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

  public navigateToDashboard() {
    this._router.navigate(['/dashboard']);
  }

  protected readonly TrackList = TrackList;
}
