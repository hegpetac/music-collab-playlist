import {Component, inject} from '@angular/core';
import {MatCard} from '@angular/material/card';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {CodeJoin} from './code-join/code-join';
import {MatButton} from '@angular/material/button';
import {JoinPlaylistReq, JoinService} from '../../../openapi';
import {Router} from '@angular/router';
import {MatError} from '@angular/material/form-field';

@Component({
  selector: 'app-landing-page',
  imports: [
    MatCard,
    MatIcon,
    CodeJoin,
    MatButton,
    MatError,
  ],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.css'
})
export class LandingPage {
  private _joinService: JoinService = inject(JoinService);
  private _router: Router = inject(Router);
  public playlistNotFound: boolean = false;

  constructor() {
    const iconRegistry = inject(MatIconRegistry);
    const domSanitizer = inject(DomSanitizer);
    iconRegistry.addSvgIcon("google-logo", domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/google.svg'));
    iconRegistry.addSvgIcon("spotify-logo", domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/spotify.svg'));
  }

  //TODO change localhost URL's
  public loginWithGoogle() {
    window.location.href = 'http://127.0.0.1:8080/oauth2/authorization/google';
  }

  public loginWithSpotify() {
    window.location.href = 'http://127.0.0.1:8080/oauth2/authorization/spotify';
  }

  public joinToSession(req: JoinPlaylistReq) : void {
    this.playlistNotFound = false;
    console.log(req);
    this._joinService.joinPlaylist(req).subscribe({
        next: (resp) => {
          console.log(resp);
          this._router.navigate(["/suggestion-dashboard"], {
            queryParams: {
              name: resp.name,
              deviceCode: resp.deviceCode,
            }
          });
        },
        error: (err) => {
          console.log('Playlist not found');
          this.playlistNotFound = true;
        }
      })
  }
}
