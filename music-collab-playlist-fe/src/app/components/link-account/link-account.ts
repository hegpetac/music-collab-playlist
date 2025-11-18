import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {MatButton} from '@angular/material/button';
import {MatCard} from '@angular/material/card';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';

@Component({
  selector: 'app-link-account',
  imports: [
    MatButton,
    MatCard,
    MatIcon
  ],
  templateUrl: './link-account.html',
  styleUrl: './link-account.css'
})
export class LinkAccount implements OnInit {

  private route = inject(ActivatedRoute);
  public requiredProviders: string[] = [];

  constructor() {
    const iconRegistry = inject(MatIconRegistry);
    const domSanitizer = inject(DomSanitizer);
    iconRegistry.addSvgIcon("google-logo", domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/google.svg'));
    iconRegistry.addSvgIcon("spotify-logo", domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/spotify.svg'));
  }

  public ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const missingParam = params['missing'];

      if (missingParam) {
        this.requiredProviders = missingParam.split(',');
      } else {
        this.requiredProviders = [];
      }
      console.log(this.requiredProviders);
    });
  }

  public linkProvider(provider: 'google' | 'spotify') {
    window.location.href = `http://127.0.0.1:8080/oauth2/authorization/${provider}`;
  }
}
