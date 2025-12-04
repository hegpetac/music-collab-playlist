import {Component, computed, effect, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Provider, SearchService, TrackSummary} from '../../../openapi';
import {debounceTime, of, switchMap} from 'rxjs';
import {MatFormField, MatInput, MatLabel, MatSuffix} from '@angular/material/input';
import {MatAutocomplete, MatAutocompleteTrigger, MatOption} from '@angular/material/autocomplete';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {DecimalPipe} from '@angular/common';
import {toObservable} from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-suggestion-dashboard',
  imports: [
    MatInput,
    MatAutocompleteTrigger,
    MatIconButton,
    MatSuffix,
    MatIcon,
    MatFormField,
    MatLabel,
    MatAutocomplete,
    MatOption,
    DecimalPipe,
    MatButton
  ],
  templateUrl: './suggestion-dashboard.html',
  styleUrl: './suggestion-dashboard.css',
})
export class SuggestionDashboard implements OnInit {
  private _route: ActivatedRoute = inject(ActivatedRoute);
  private _searchService = inject(SearchService);
  public deviceCode!: number;
  public playlistName!: string;

  public spotifyQuery = signal<string>("");
  public youtubeQuery = signal<string>("");
  public spotifyResults = signal<TrackSummary[]>([]);
  public youtubeResults = signal<TrackSummary[]>([]);
  public selectedSpotify = signal<TrackSummary | null>(null);
  public selectedYoutube = signal<TrackSummary | null>(null);
  public spotifyQueryDebounced = computed(() => this.spotifyQuery().trim());
  public youtubeQueryDebounced = computed(() => this.youtubeQuery().trim());

  constructor() {
    const spotify$ = toObservable(this.spotifyQueryDebounced);
    const youtube$ = toObservable(this.youtubeQueryDebounced);

    effect(() => {
      spotify$
        .pipe(
          debounceTime(400),
          switchMap(query =>
            query.trim().length > 0 ?
              this._searchService.searchMusic({
                deviceCode: this.deviceCode,
                playlistName: this.playlistName,
                provider: Provider.Spotify,
                query: query,
              }) : of([])
          )
        ).subscribe(res => this.spotifyResults.set(res));
    });

    effect(() => {
      youtube$
        .pipe(
          debounceTime(400),
          switchMap(query =>
            query.trim().length > 0 ?
              this._searchService.searchMusic({
                deviceCode: this.deviceCode,
                playlistName: this.playlistName,
                provider: Provider.Youtube,
                query: query,
              }) : of([])
          )
        ).subscribe(res => this.youtubeResults.set(res));
    });
  }

  public ngOnInit(): void {
    this.deviceCode = Number.parseInt(this._route.snapshot.paramMap.get('deviceCode') ?? "0");
    this._route.queryParams.subscribe(params => {
      this.deviceCode = params['deviceCode'];
      this.playlistName = params['name'];
    });
    console.log(this.playlistName);
    console.log(this.deviceCode);


  }

  public displayTrack(track: TrackSummary | null): string {
    if (!track) return '';
    return track.artist ? `${track.title} - ${track.artist}` : track.title;
  }

  public onSpotifySelected(track: TrackSummary) {
    this.selectedSpotify.set(track);
    this.spotifyQuery.set(this.displayTrack(track));
  }

  public onYoutubeSelected(track: TrackSummary | null) {
    this.selectedYoutube.set(track);
    this.youtubeQuery.set(this.displayTrack(track));
  }

  public clearSpotifySelection() {
    this.selectedSpotify.set(null);
    this.spotifyQuery.set('');
    this.spotifyResults.set([]);
  }

  public clearYouTubeSelection() {
    this.selectedYoutube.set(null);
    this.youtubeQuery.set('');
    this.youtubeResults.set([]);
  }

  public suggestSpotify() {
    const track = this.selectedSpotify();
    if (!track) return;
    console.log(track)
    //TODO suggestService
  }

  public suggestYoutube() {
    const track = this.selectedYoutube();
    if (!track) return;
    console.log(track)
    //TODO suggestService
  }
}
