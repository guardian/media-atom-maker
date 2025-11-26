export const videoPlayerFormats: {id: VideoPlayerFormat, title: string}[] = [
  {id: "Loop", title: "Loop"},
  {id: "Default", title: "Standard"},
  {id: "Cinemagraph", title: "Cinemagraph"}
];

export type VideoPlayerFormat = "Loop" | "Default" | "Cinemagraph";

export type VideoPlayerOption = VideoPlayerFormat | 'Youtube';
