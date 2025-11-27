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
          title: "Youtube",
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
              "Don't use for sensitive content",
              "No share button"
            ],
            info: [
              "Ideal for long-form content with a wide audience," +
              "Can be used anywhere",
              "Hosted on Youtube"
            ]
          }
        },
      ],
      "selfHosted": [
        {
          id: "Loop",
          title: "Loop",
          specifications: {
            positive: [
              "Autoplays (if not disabled by user’s accessibility preferences or low battery mode)",
              "Loops",
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
              "Can be used on Fronts only (as of Nov 2025)",
              "Self-hosted"
            ]
          }
        },
        {
          id: "Cinemagraph",
          title: "Cinemagraph",
          specifications: {
            positive: [
              "Autoplays (if not disabled by user’s accessibility preferences or low battery mode)",
              "Loops",
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
              "No Guardian player (as of Nov 2025)",
              "No subtitles support (as of Nov 2025)",
              "No share button (as of Nov 2025)",
              "No support for livestreaming"
            ],
            info: [
              "Used for sensitive content and legacy videos",
              "Can be used in Articles only (as of Nov 2025)",
              "Self-hosted"
            ]
          }
        }
      ]
  };
