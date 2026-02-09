export interface Seat {
  id: number;
  seatNumber: string;
  status: 'AVAILABLE' | 'RESERVED_RANDOM' | 'RESERVED_USER';
}
