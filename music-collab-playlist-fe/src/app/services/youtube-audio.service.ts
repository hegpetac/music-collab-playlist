import {Injectable} from '@angular/core';
import * as YT from 'youtube';

declare global {
  interface Window {
    YT: any;
    onYouTubeIframeAPIReady: () => void;
  }
}

@Injectable({
  providedIn: 'root'
})
export class YoutubeAudioService {
  private player!: YT.Player;
  private apiReady = false;
  private playerReady = false;

  private currentVideo: string | null  = null;

  constructor() {
    this.waitForFirstInteraction();
  }

  private waitForFirstInteraction() {
    document.addEventListener('click', () => {
      this.loadYoutubeApi()
    }, {
      once: true
    });
  }

  private loadYoutubeApi(): void {
    if (window.YT && window.YT.Player) {
      this.apiReady = true;
      this.createHiddenPlayer();
      return;
    }

    const tag = document.createElement('script');
    tag.src = 'https://www.youtube.com/iframe_api';

    window.onYouTubeIframeAPIReady = () => {
      this.apiReady = true;
      this.createHiddenPlayer();
    };

    document.body.appendChild(tag);
  }

  private createHiddenPlayer(): void {
    const container = document.createElement('div');

    container.id = "hidden-youtube-player";

    container.style.position = 'fixed';
    container.style.left = '0';
    container.style.top = '0';
    container.style.width = '200px';
    container.style.height = '200px';
    container.style.zIndex = '-100';

    container.style.pointerEvents = 'none';

    document.body.appendChild(container);

    this.player = new window.YT.Player("hidden-youtube-player", {
      height: '200',
      width: '200',
      videoId: '',
      playerVars: {
        autoplay: 1,
        controls: 0,
        disablekb: 1,
        fs: 0,
        modestbranding: 1,
        rel: 0,
      },
      events: {
        onReady: () => {
          this.playerReady = true;
          this.player.setVolume(100);
        }
      }
    });
  }

  private waitForReady(): Promise<void> {
    return new Promise(resolve => {
      const check = () => {
        if (this.apiReady && this.playerReady) {
          resolve();
        } else {
          setTimeout(check, 100);
        }
      };

      check();
    });
  }

  async play(videoId: string, startSeconds: number): Promise<void> {
    await this.waitForReady();

    this.currentVideo = videoId;

    console.log(this.currentVideo);

    this.player.loadVideoById({
      videoId,
      startSeconds,
    })

    this.player.playVideo();
  }

  pause(): void {
    if (!this.playerReady) return;

    this.player.stopVideo();
  }
}
