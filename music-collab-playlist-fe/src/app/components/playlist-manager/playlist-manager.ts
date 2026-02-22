import {Component, inject, OnInit} from '@angular/core';
import {
  DashboardService,
  DashboardSettings,
  HandlePlaylistService, Provider,
  TrackSummary
} from '../../../openapi';
import {WebSocketService} from '../../services/websocket.service';
import {combineLatest} from 'rxjs';
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {TrackContainer} from './track-container/track-container';

@Component({
  selector: 'app-playlist-manager',
  imports: [
    CdkDropList,
    TrackContainer
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

  public ngOnInit() {
    combineLatest([
      this._dashboardService.getDashboardSettings(),
      this._ws.isConnected()
    ])
    .subscribe(([settings]) => {
      this.settings = settings;
      console.log(settings);
      this._ws.subscribe<TrackSummary[]>(
        `/topic/suggestions/${this.settings?.name}`,
        (data: TrackSummary[]) => {
          this.suggestions = data;
          console.log(data);
        }
      )
      this._ws.subscribe<TrackSummary[]>(
        `/topic/queue/${this.settings?.name}`,
        (data: TrackSummary[]) => {
          this.queue = data;
          console.log(data);
        }
      )
      this._ws.subscribe<TrackSummary[]>(
        `/topic/recommendations/${this.settings?.name}`,
        (data: TrackSummary[]) => {
          this.recommendations = data;
          console.log(data);
        }
      )
    })
    this.suggestions = [
      {
        provider: Provider.Spotify,
        providerId: "1",
        title: "Nem Origó",
        artist: "Ákos",
        album: "Origo??",
        durationMs: 4556,
        thumbnail: "https://i.ytimg.com/vi/nSOMOXcviZc/default.jpg"
      },
      {
        provider: Provider.Spotify,
        providerId: "1",
        title: "Origó",
        artist: "Pápai Jóci",
        album: "Origo??",
        durationMs: 4556,
        thumbnail: "https://i.scdn.co/image/ab67616d0000b2733d471b0bc3892254f52163fd"
      },
      {
        provider: Provider.Spotify,
        providerId: "1",
        title: "introvertált dal",
        artist: "Azi deshi",
        album: "Origo??",
        durationMs: 4556,
      },
      {
        provider: Provider.Spotify,
        providerId: "6",
        title: "MAJKAAKAKKA",
        artist: "Mit csinálok az életemmel",
        album: "Origo??",
        durationMs: 4556,
        thumbnail: "https://i.scdn.co/image/ab67616d0000b2733d471b0bc3892254f52163fd"
      },
    ];

    this.queue = [
      {
        provider: Provider.Spotify,
        providerId: "6",
        title: "MAJKAAKAKKA",
        artist: "Mit csinálok az életemmel",
        album: "Origo??",
        durationMs: 4556,
        thumbnail: "https://i.scdn.co/image/ab67616d0000b2733d471b0bc3892254f52163fd"
      }
    ]

    this.recommendations = [
      {
        provider: Provider.Spotify,
        providerId: "1",
        title: "Origó",
        artist: "Pápai Jóci",
        album: "Origo??",
        durationMs: 4556,
        thumbnail: "https://i.scdn.co/image/ab67616d0000b2733d471b0bc3892254f52163fd"
      }
    ]
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
      this._playlistHandlerService.addRecommendedTrack({
        track: this.queue.at(event.currentIndex),
        order: this.queue.map(track => {
          return {
            providerId: track.providerId,
            provider: track.provider
          }
        })
      })
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      )
      this._playlistHandlerService.addSuggestedTrack({
        track: this.queue.at(event.currentIndex),
        order: this.queue.map(track => {
          return {
            providerId: track.providerId,
            provider: track.provider
          }
        })
      })
    }
  }

  public deleteTrackFromList(track: TrackSummary, list: TrackSummary[]): void {
    list.splice(list.indexOf(track), 1);
  }
}
