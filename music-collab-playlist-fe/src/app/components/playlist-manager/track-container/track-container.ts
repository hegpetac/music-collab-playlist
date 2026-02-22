import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TrackSummary} from '../../../../openapi';
import {DecimalPipe} from '@angular/common';
import {CdkDrag} from '@angular/cdk/drag-drop';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-track-container',
  imports: [
    DecimalPipe,
    CdkDrag,
    MatIconButton,
    MatIcon
  ],
  templateUrl: './track-container.html',
  styleUrl: './track-container.css',
})
export class TrackContainer {
  @Input({required: true}) item!: TrackSummary

  @Output() deleteTrack = new EventEmitter<TrackSummary>();

  public emitDelete(track: TrackSummary): void {
    this.deleteTrack.emit(track);
  }
}
