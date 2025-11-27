export const videoPlayerFormats: {id: VideoPlayerFormat, title: string}[] = [
  {id: "Loop", title: "Loop"},
  {id: "Cinemagraph", title: "Cinemagraph"},
  {id: "Default", title: "Standard"},
];

export type VideoPlayerFormat = "Loop" | "Cinemagraph" | "Default";

export type VideoPlayerOption = VideoPlayerFormat | 'Youtube';
