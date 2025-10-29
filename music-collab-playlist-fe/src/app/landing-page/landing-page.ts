import {Component, inject} from '@angular/core';
import {MatCard} from '@angular/material/card';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {CodeJoin} from './code-join/code-join';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'app-landing-page',
  imports: [
    MatCard,
    MatIcon,
    CodeJoin,
    MatButton,
  ],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.css'
})
export class LandingPage {
  constructor() {
    const iconRegistry = inject(MatIconRegistry);
    const domSanitizer = inject(DomSanitizer);
    iconRegistry.addSvgIcon("google-logo", domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/google.svg'));
    iconRegistry.addSvgIcon("spotify-logo", domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/spotify.svg'));
  }
}
