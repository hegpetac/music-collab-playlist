import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'msToTime',
})
export class MsToTimePipe implements PipeTransform {

  transform(value: number | null | undefined): string {
    if (value == null || isNaN(value)) {
      return '00:00'
    }

    const totalSeconds = Math.floor(value / 1000);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return minutes.toString().padStart(2, '0') + ':' + seconds.toString().padStart(2, '0');
  }

}
