import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'minutesToTime',
})
export class MinutesToTimePipe implements PipeTransform {

  public transform(value: number | null | undefined): string {
    if (value == null || isNaN(value)) {
      return '0:00'
    }

    const hours = Math.floor(value / 60);
    const mimutes = value % 60;

    return hours.toString().padStart(1, '0') + ':' + mimutes.toString().padStart(2, '0');
  }

}
