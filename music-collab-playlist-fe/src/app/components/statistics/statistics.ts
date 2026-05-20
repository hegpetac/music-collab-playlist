import {Component, inject, OnInit, ViewChild} from '@angular/core';
import {MostPlayedTrack, PlaylistStatistics, StatisticsService} from '../../../openapi';
import {MatCard} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {MinutesToTimePipe} from '../../pipes/minutes-to-time-pipe';
import {BaseChartDirective} from 'ng2-charts';
import {ChartConfiguration} from 'chart.js';
import {MatButton} from '@angular/material/button';
import {Router} from '@angular/router';
import {PlaybackManager} from '../playback-manager/playback-manager';

@Component({
  selector: 'app-statistics',
  imports: [
    MatCard,
    MatIcon,
    MinutesToTimePipe,
    BaseChartDirective,
    MatButton,
    PlaybackManager
  ],
  templateUrl: './statistics.html',
  styleUrl: './statistics.css',
})
export class Statistics implements OnInit {
  private readonly _statisticsService: StatisticsService = inject(StatisticsService);
  private _router: Router = inject(Router);

  @ViewChild(BaseChartDirective)
  chart?: BaseChartDirective;

  public statistics!: PlaylistStatistics;

  public pieChartData: ChartConfiguration<'pie'>['data'] = {
    labels: ['Spotify', 'YouTube'],
    datasets: [
      {
        data: [10, 0]
      }
    ]
  }
  public pieChartOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    plugins: {
      legend: {
        position: 'right'
      }
    }
  }
  public mostPlayedTrack: MostPlayedTrack | null = null;

  ngOnInit(): void {
    this._statisticsService.getPlaylistStatistics().subscribe(resp => {
      this.statistics = resp.playlistStatistics!;
      this.mostPlayedTrack = this.statistics.mostPlayedTrack!;
      this.pieChartData.datasets[0].data = [this.statistics.spotifyMinutesPlayed!, this.statistics.youtubeMinutesPlayed!]
      this.chart?.update();
    })
  }

  public navigateToPlaylist() {
    this._router.navigate(['/playlist-manager']);
  }

  public navigateToDashboard() {
    this._router.navigate(['/dashboard']);
  }
}
