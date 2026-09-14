import dayjs from 'dayjs';

export function formatDateTime(value: string | number | Date) {
  return dayjs(value).format('YYYY-MM-DD HH:mm');
}
