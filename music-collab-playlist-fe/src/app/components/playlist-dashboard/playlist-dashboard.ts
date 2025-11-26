import {Component, inject, OnInit} from '@angular/core';
import {MatCard} from '@angular/material/card';
import {
  Configuration,
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
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs';

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
  ],
  providers: [
    {
      provide: Configuration,
      useValue: new Configuration({ withCredentials: true })
    },
    DashboardService
  ],
  templateUrl: './playlist-dashboard.html',
  styleUrl: './playlist-dashboard.css',
})
export class PlaylistDashboard implements OnInit {
  private _dashboardService: DashboardService = inject(DashboardService);
  private _formbuilder: FormBuilder = inject(FormBuilder);

  public settings: DashboardSettings | null = null;
  public isRegenerating = false;
  public dashboardForm!: FormGroup<IDashboardSettingsForm>;

  public ngOnInit(): void {
    this.loadSettings();
  }

  public loadSettings(): void {
    this._dashboardService.getDashboardSettings().subscribe({
      next: (s) => {
        this.settings = s;
        this.dashboardForm = this.buildSettingsForm(s);
        this.subscribeToFormControlValueChanges();
      },
      error: (err) => {
        console.error('Failed to load settings', err);
      }
    });
  }

  public buildSettingsForm(s: DashboardSettings): FormGroup<IDashboardSettingsForm> {
    return this._formbuilder.group<IDashboardSettingsForm>({
      playlistName: this._formbuilder.nonNullable.control(s.name ?? "", [Validators.required]),
      suggestionPlaybackMode: this._formbuilder.nonNullable.control(s.suggestionPlaybackMode ?? SuggestionPlaybackMode.CollectSuggestions),
      youtubePlaybackMode: this._formbuilder.nonNullable.control(s.youtubePlaybackMode ?? YoutubePlaybackMode.BuiltIn)
    })
  }

  public subscribeToFormControlValueChanges(): void {
    this.dashboardForm.controls.suggestionPlaybackMode.valueChanges.subscribe(value => {
      const req: ModifySuggestionPlaybackModeReq = {suggestionPlaybackMode: value};
      this._dashboardService.setSuggestionPlayback(req).subscribe({
        next: (updatedMode) => {
          if (this.settings) {
            this.settings.suggestionPlaybackMode = updatedMode;
          }
        },
        error: (err) => console.error('Failed to update suggestion playback mode', err)
      });
    })

    this.dashboardForm.controls.youtubePlaybackMode.valueChanges.subscribe(value => {
      const req: ModifyYoutubePlaybackModeReq = {youtubePlaybackMode: value};
      this._dashboardService.setYoutubePlayback(req).subscribe({
        next: (updatedMode) => {
          if (this.settings) {
            this.settings.youtubePlaybackMode = updatedMode;
          }
        },
        error: (err) => console.error('Failed to update YouTube playback mode', err)
      });
    })

    this.dashboardForm.controls.playlistName.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(value => {
      if (value.trim() !== "") {
        this._dashboardService.setName({name: value}).subscribe({
          next: (updatedNameResp) => {
            if (this.settings) {
              this.settings.name = updatedNameResp.name;
            }
          }
        })
      }
    });
  }

  public changeProvider(provider: Provider) {
    window.location.href = `http://127.0.0.1:8080/oauth2/authorization/${provider}`;
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
}

export interface IDashboardSettingsForm {
  playlistName: FormControl<string>;
  suggestionPlaybackMode: FormControl<SuggestionPlaybackMode>;
  youtubePlaybackMode: FormControl<YoutubePlaybackMode>;
}
