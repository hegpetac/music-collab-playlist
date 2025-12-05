import {Component, inject, OnInit} from '@angular/core';
import {DashboardService, DashboardSettings, TrackSummary} from '../../../openapi';
import {WebSocketService} from '../../services/websocket.service';
import {combineLatest, combineLatestAll, combineLatestWith} from 'rxjs';

@Component({
  selector: 'app-playlist-manager',
  imports: [],
  templateUrl: './playlist-manager.html',
  styleUrl: './playlist-manager.css',
})
export class PlaylistManager implements OnInit {
  public suggestions: TrackSummary[] = [];
  public settings: DashboardSettings | null = null;

  private _ws: WebSocketService = inject(WebSocketService);
  private _dashboardService: DashboardService = inject(DashboardService);

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
    })
  }
}
