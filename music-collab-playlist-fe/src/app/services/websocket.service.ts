import {Injectable} from '@angular/core';
import {Client} from '@stomp/stompjs';
import {BehaviorSubject, Observable} from 'rxjs';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client!: Client;
  private connected$ = new BehaviorSubject<boolean>(false);

  constructor() {
    this.connect();
  }

  private connect() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      debug: () => {}
    })

    this.client.onConnect = () => {
      this.connected$.next(true);
      console.log('WS Connected!');
    }

    this.client.onStompError = frame => {
      console.error('Broker error: ', frame.headers['message']);
    };

    this.client.activate();
  }

  public subscribe<T>(topic: string, handler: (body: T) => void) {
    this.client.subscribe(topic, (message) => {
      handler(JSON.parse(message.body) as T);
    })
  }

  public isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }
}
