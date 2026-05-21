import { Routes } from '@angular/router';
import {LandingPage} from './components/landing-page/landing-page';
import {LinkAccount} from './components/link-account/link-account';
import {PlaylistDashboard} from './components/playlist-dashboard/playlist-dashboard';
import {PlaylistManager} from './components/playlist-manager/playlist-manager';
import {SuggestionDashboard} from './components/suggestion-dashboard/suggestion-dashboard';
import {Statistics} from './components/statistics/statistics';
import {OwnerFlow} from './components/owner-flow/owner-flow';

export const routes: Routes = [
  {
    path: '',
    component: LandingPage
  },
  {
    path: 'dashboard',
    component: OwnerFlow,

    children: [
      {
        path: '',
        component: PlaylistDashboard
      },
      {
        path: 'playlist-manager',
        component: PlaylistManager
      },
      {
        path: 'statistics',
        component: Statistics
      }
    ]
  },
  {
    path: 'link-account',
    component: LinkAccount
  },
  {
    path: 'suggestion-dashboard',
    component: SuggestionDashboard
  },
];
