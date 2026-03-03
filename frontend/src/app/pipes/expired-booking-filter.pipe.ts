import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'expiredBookingFilter',
  standalone: true,
  pure: false,
})
export class ExpiredBookingFilterPipe implements PipeTransform {
  private readonly MOVIE_DURATION_HOURS = 2.5;
  private readonly EXPIRY_HOURS = 5;

  transform(bookings: any[]): any[] {
    if (!bookings || bookings.length === 0) {
      return bookings;
    }

    const now = new Date();

    return bookings.filter((booking) => {
      const showtimeStart = this.parseArgentinaDate(booking.showDateTime);
      const showtimeEnd = new Date(
        showtimeStart.getTime() + this.MOVIE_DURATION_HOURS * 60 * 60 * 1000,
      );
      const cutoffTime = new Date(showtimeEnd.getTime() + this.EXPIRY_HOURS * 60 * 60 * 1000);

      return cutoffTime > now;
    });
  }

  private parseArgentinaDate(dateString: string): Date {
    if (!dateString) return new Date();

    const hasTimezone =
      dateString.includes('Z') || dateString.includes('+') || dateString.includes('-', 11);

    const correctedDateString = hasTimezone ? dateString : dateString + '-03:00';
    return new Date(correctedDateString);
  }
}
