export type VideoPlayerFormat = "Loop" | "Cinemagraph" | "Default";

export type VideoCreateOption = VideoPlayerFormat | 'Youtube';

export type VideoCreateOptionDetails = {
  id: VideoCreateOption,
  title: string,
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
              "Ideal for long-form content with a wide audience",
              "Can be used anywhere",
              "Hosted on Youtube"
            ]
          }
        }
      ],
      "selfHosted": [
        {
          id: "Loop",
          title: "Loop",
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
              "Ideal for short, semantic clips",
              "Can be used on Fronts only",
              "Self-hosted"
            ]
          }
        },
        {
          id: "Cinemagraph",
          title: "Cinemagraph",
          specifications: {
            positive: [
              "Autoplays and loops",
              "Click through to content"
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
              "Ideal for short, decorative clips",
              "Can be used on Fronts only",
              "Self-hosted"
            ]
          }
        },
        {
          id: "Default",
          title: "Standard",
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
              "No Guardian player",
              "No subtitles support",
              "No share button",
              "No support for livestreaming"
            ],
            info: [
              "Use for content we want to use without YouTube's age restrictions",
              "Can be used in Articles only",
              "Self-hosted"
            ]
          }
        }
      ]
  };
