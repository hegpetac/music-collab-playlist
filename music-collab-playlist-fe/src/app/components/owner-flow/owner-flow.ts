import { Component } from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {PlaybackManager} from '../playback-manager/playback-manager';

@Component({
  selector: 'app-owner-flow',
  imports: [
    RouterOutlet,
    PlaybackManager
  ],
  templateUrl: './owner-flow.html',
  styleUrl: './owner-flow.css',
})
export class OwnerFlow {

}
