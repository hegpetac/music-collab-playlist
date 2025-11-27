import { Routes } from '@angular/router';
import {LandingPage} from './components/landing-page/landing-page';
import {LinkAccount} from './components/link-account/link-account';
import {PlaylistDashboard} from './components/playlist-dashboard/playlist-dashboard';
import {PlaylistManager} from './components/playlist-manager/playlist-manager';
import {SuggestionDashboard} from './components/suggestion-dashboard/suggestion-dashboard';

export const routes: Routes = [
  {
    path: '',
    component: LandingPage
  },
  {
    path: 'link-account',
    component: LinkAccount
  },
  {
    path: 'dashboard',
    component: PlaylistDashboard
  },
  {
    path: 'playlist-manager',
    component: PlaylistManager
  },
  {
    path: 'suggestion-dashboard',
    component: SuggestionDashboard
  }
];
