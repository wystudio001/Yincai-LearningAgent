export interface ModelProvider {
  id: string;
  name: string;
  baseUrl: string;
  model: string;
  keyMasked: string;
  enabled: boolean;
}
