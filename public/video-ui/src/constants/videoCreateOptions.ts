export type VideoPlayerFormat = "Loop" | "Cinemagraph" | "Default";

export type VideoCreateOption = VideoPlayerFormat | 'Youtube';

export type VideoCreateOptionDetails = {
  id: VideoCreateOption,
  title: string,
  description: string,
  specifications: {
    positive: string[],
    negative: string[],
    info: string[]
  }
}

export const videoCreateOptions: {
  "offPlatform": VideoCreateOptionDetails[],
  "selfHosted": VideoCreateOptionDetails[]
} = {
      "offPlatform": [
        {
          id: "Youtube",
          title: "YouTube",
          description: "Use on platform (non age-restricted content) and on YouTube",
          specifications: {
            positive: [
              "Manual play / pause",
              "Optional video page",
              "Progress bar and timeline scrubber",
              "Audio track and mute controls",
              "Supports subtitles (through YouTube)",
              "Fullscreen controls",
              "Livestreaming support"
            ],
            negative: [
              "No share button",
              "Age restrictions may apply"
            ],
            info: [
              "Can be used on Fronts and Articles",
              "Hosted on Youtube"
            ]
          }
        }
      ],
      "selfHosted": [
        {
          id: "Loop",
          title: "Loop",
          description: "Short looping video – use for journalism (eg Breaking News)",
          specifications: {
            positive: [
              "Autoplays and loops",
              "Play / pause controls",
              "Progress bar",
              "Audio track and mute controls",
              "Supports subtitles"
            ],
            negative: [
              "No timeline scrubber",
              "No fullscreen controls",
              "No dedicated video page",
              "No share button"
            ],
            info: [
              "Can be used on Fronts and Articles",
              "Self-hosted"
            ]
          }
        },
        {
          id: "Cinemagraph",
          title: "Cinemagraph",
          description: "Short GIF-like video – use for illustration (eg Features)",
          specifications: {
            positive: [
              "Autoplays and loops",
              "User interaction clicks through to article"
            ],
            negative: [
              "No play / pause controls",
              "No progress bar and timeline scrubber",
              "No audio track and subtitles support",
              "No fullscreen controls",
              "No dedicated video page",
              "No share button"
            ],
            info: [
              "Can be used on Fronts only",
              "Self-hosted"
            ]
          }
        },
        {
          id: "Default",
          title: "Non-YouTube",
          description: "Use in limited circumstances (eg avoiding age restriction on platform)",
          specifications: {
            positive: [
              "Manual play",
              "Optional dedicated video page",
              "Play / pause controls",
              "Progress bar and timeline scrubber",
              "Audio track and mute controls",
              "Fullscreen controls"
            ],
            negative: [
              "No subtitles support",
              "No share button",
              "No support for livestreaming"
            ],
            info: [
              "Can be used in Articles only",
              "Self-hosted",
              "Different browsers (eg Firefox, Chrome) will use their own player to render these videos"
            ]
          }
        }
      ]
  };
