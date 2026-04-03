import {Component, computed, effect, inject, signal} from '@angular/core';
import {HandlePlaylistService, Provider, SearchService, TrackSummary} from '../../../../openapi';
import {toObservable} from '@angular/core/rxjs-interop';
import {debounceTime, of, switchMap} from 'rxjs';
import {DecimalPipe} from '@angular/common';
import {MatAutocomplete, MatAutocompleteTrigger, MatOption} from '@angular/material/autocomplete';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatFormField, MatInput, MatLabel, MatSuffix} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-search',
  imports: [
    DecimalPipe,
    MatAutocomplete,
    MatAutocompleteTrigger,
    MatButton,
    MatFormField,
    MatIcon,
    MatIconButton,
    MatInput,
    MatLabel,
    MatOption,
    MatSuffix
  ],
  templateUrl: './search.html',
  styleUrl: './search.css',
})
export class Search {
  private _searchService = inject(SearchService);
  private _playlistHandlerService = inject(HandlePlaylistService);

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
              this._searchService.searchAsOwner({
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
              this._searchService.searchAsOwner({
                provider: Provider.Youtube,
                query: query,
              }) : of([])
          )
        ).subscribe(res => this.youtubeResults.set(res));
    });
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

  public addTrackFromSpotify() {
    const track = this.selectedSpotify();
    if (!track) return;
    this._playlistHandlerService.addTrack({
      track: track,
    }).subscribe();
    this.clearSpotifySelection();
  }

  public addTrackFromYoutube() {
    const track = this.selectedYoutube();
    if (!track) return;
    this._playlistHandlerService.addTrack({
      track: track,
    }).subscribe();
    this.clearYouTubeSelection();
  }
}
