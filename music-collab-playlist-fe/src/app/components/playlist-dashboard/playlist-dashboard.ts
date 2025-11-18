import {Component, inject, OnInit} from '@angular/core';
import {MatCard} from '@angular/material/card';
import {
  DashboardService,
  DashboardSettings,
  ModifySuggestionPlaybackModeReq,
  ModifyYoutubePlaybackModeReq,
  SuggestionPlaybackMode,
  YoutubePlaybackMode
} from '../../../openapi';
import {MatButton} from '@angular/material/button';
import {MatRadioButton, MatRadioGroup} from '@angular/material/radio';
import {QRCodeComponent} from 'angularx-qrcode';
import {MatFormField} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormControl, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {debounceTime, distinctUntilChanged, Subject} from 'rxjs';

@Component({
  selector: 'app-playlist-dashboard',
  imports: [
    MatCard,
    MatButton,
    MatRadioGroup,
    MatRadioButton,
    QRCodeComponent,
    MatFormField,
    MatInput,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './playlist-dashboard.html',
  styleUrl: './playlist-dashboard.css',
})
export class PlaylistDashboard implements OnInit {
  private _dashboardService: DashboardService = inject(DashboardService);
  private _keyupSubject = new Subject<string>();

  public settings: DashboardSettings | null = null;
  public isRegenerating = false;

  public ngOnInit(): void {
    this.loadSettings();
    this._keyupSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(name => {
      this._dashboardService.setName({ name: name }).subscribe(name => {this.settings!.name = name });
    })
  }

  public loadSettings(): void {
    // console.log('Loading mock data for development')
    // this.settings = {
    //   name: "my-asd",
    //   spotifyAccountDisplayName: "my-account",
    //   googleAccountEmail: "my@email.com",
    //   suggestionPlaybackMode: SuggestionPlaybackMode.CollectSuggestions,
    //   youtubePlaybackMode: YoutubePlaybackMode.BuiltIn,
    //   deviceCode: 123456,
    //   qrBaseUrl: "http://localhost:4200/join?name=my-dashboard&code=123456"
    // }
    this._dashboardService.getDashboardSettings().subscribe({
      next: (s) => {
        this.settings = s;
      },
      error: (err) => {
        console.error('Failed to load settings', err);
      }
    });
  }

  public changeProvider(provider: Provider) {
    window.location.href = `http://127.0.0.1:8080/oauth2/authorization/${provider}`;
  }

  public onPlaybackChange(mode: SuggestionPlaybackMode) {
    const req: ModifySuggestionPlaybackModeReq = {suggestionPlaybackMode: mode};
    this._dashboardService.setSuggestionPlayback(req).subscribe({
      next: (updatedMode) => {
        if (this.settings) {
          this.settings.suggestionPlaybackMode = updatedMode;
        }
      },
      error: (err) => console.error('Failed to update suggestion playback mode', err)
    });
  }

  public onYouTubeModeChange(mode: YoutubePlaybackMode) {
    const req: ModifyYoutubePlaybackModeReq = {youtubePlaybackMode: mode};
    this._dashboardService.setYoutubePlayback(req).subscribe({
      next: (updatedMode) => {
        if (this.settings) {
          this.settings.youtubePlaybackMode = updatedMode;
        }
      },
      error: (err) => console.error('Failed to update YouTube playback mode', err)
    });
  }

  public regenerateCode() {
    this.isRegenerating = true;
    this._dashboardService.regenerateDeviceCode().subscribe({
      next: (resp) => {
        if (this.settings) {
          this.settings.deviceCode = resp.deviceCode;
          this.settings.qrBaseUrl = resp.qrBaseUrl;
        }
        this.isRegenerating = false;
      },
      error: (err) => {
        console.error('Failed to regenerate device code', err);
        this.isRegenerating = false;
      }
    });
  }

  public nameInputChanged() {
    this._keyupSubject.next(this.settings!.name!);
  }
}
