package bio.overture.song.server.utils.jwt;

import bio.overture.song.server.security.PublicKeyFetcher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"test", "jwt"})
public class TestPublicKeyFetcher implements PublicKeyFetcher {

  private static final String PUBLIC_KEY =
      "-----BEGIN PUBLIC KEY-----\n"
      + "\n"
      + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoeOWCkHqvK4bnyaPLNepJKwPg4+jUWitow7lA8h5ml45GwjVAuWJhERNVh1yxBS9Id9nNhIC2g6Ds6dZEm/T5BXnURDbRSVyq5MCuLjLDHtTYkrmKSFQpzbJp8v+pNnmeP0WLpFa574v5mAyHNBYuvDS7Bzwoz423BbAL8h6aJQSql4LbuNC1W7kRfpx7CkMF7h6hgakcbE9ti0owseZ9LEnlzR8Lg+KCneG2/QnBhsjCW2I1PBtLFNtEiZWA6G/rqbNLm7yU6jBHnJjtneYZ3arqXgsag6IrTnyLvNxhIJntz72iDOHmJlI/BepY42EGhiguaEy0ZE8jPG4mLpCuQIDAQAB\n"
      + "\n"
      + "-----END PUBLIC KEY-----";

  private static final String PRIVATE_KEY =
      "-----BEGIN PRIVATE KEY-----\n"
          + "\n"
          + "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDSU6oy48sJW6xzqzOSU1dAvUUeFKQSBHsCf7wGWUGpOxEczhtFiiyx4YUJtg+fyvwWxa4wO3GnQLBPIxBHY8JsnvjQN2lsTUoLqMB9nGpwF617uA/S2igm1u+cDpfi82kbi6SG1Sg30PM047R6oxTRGDLLkeMRF1gRaTBM0HfSL0j6ccU5KPgwYsFLE2We6jeR56iYJGC2KYLH4v8rcc2jRAdMbUntHMtUByF9BPSW7elQnyQH5Qzr/o0b59XLKwnJFn2Bp2yviC8cdyTDyhQGna0e+oESQR1j6u3Ux/mOmm3slRXscA8sH+pHmOEAtjYVf/ww36U8uZv+ctBCJyFVAgMBAAECggEBALrEeJqAFUfWFCkSmdUSFKT0bW/svFUTjXgGnZy1ncz9GpENpMH3lQDQVibteKpYwcom+Cr0XlQ66VUcudPrDjcOY7vhuMfnSh1YWLYyM4IeRHtcUxDVkFoM+vEFNHLf2zIOqqbgmboW3iDVIurT7iRO7KxAe/YtWJL9aVqMtBn7Lu7S7OvAU4ji5iLIBxjl82JYA+9lu/aQ6YGaoZuSO7bcU8Sivi+DKAahqN9XMKiB1XpC+PpaS/aec2S7xIlTdzoDGxEALRGlMe+xBEeQTBVJHBWrRIDPoHLTREeRC/9Pp+1Y4Dz8hd5Bi0n8/5r/q0liD+0vtmjsdU4E2QrktYECgYEA73qWvhCYHPMREAFtwz1mpp9ZhDCW6SF+njG7fBKcjz8OLcy15LXiTGc268ewtQqTMjPQlm1n2C6hGccGAIlMibQJo3KZHlTs125FUzDpTVgdlei6vU7M+gmfRSZed00J6jC04/qMR1tnV3HME3np7eRTKTA6Ts+zBwEvkbCetSkCgYEA4NY5iSBO1ybouIecDdD15uI2ItLPCBNMzu7IiK7IygIzuf+SyKyjhtFSR4vEi0gScOM7UMlwCMOVU10e4nMDknIWCDG9iFvmIEkGHGxgRrN5hX1Wrq74wF212lvvagH1IVWSHa8cVpMe+UwKu5Q1h4yzuYt6Q9wPQ7Qtn5emBE0CgYB2syispMUA9GnsqQii0Xhj9nAEWaEzhOqhtrzbTs5TIkoA4Yr3BkBY5oAOdjhcRBWZuJ0XMrtaKCKqCEAtW+CYEKkGXvMOWcHbNkkeZwv8zkQ73dNRqhFnjgVn3RDNyV20uteueK23YNLkQP+KV89fnuCpdcIw9joiqq/NYuIHoQKBgB5WaZ8KH/lCA8babYEjv/pubZWXUl4plISbja17wBYZ4/bl+F1hhhMr7Wk//743dF2NG7TT6W0VTvHXr9IoaMP65uQmKgfbNpsGn294ZClGEFClz+t0KpZyTpZvL0fjibr8u+GLfkxkP5qt2wjif7KRlrKjklTTva+KAVn2cW1FAoGBAMkX9ekIwhx/7uY6ndxKl8ZMDerjr6MhV0b08hHp3RxHbYVbcpN0UKspoYvZVgHwP18xlDij8yWRE2fapwgi4m82ZmYlg0qqJmyqIU9vBB3Jow903h1KPQrkmQEZxJ/4H8yrbgVf2HT+WUfjTFgaDZRl01bI3YkydCw91/Ub9HU6"
          + "\n"
          + "\n"
          + "-----END PRIVATE KEY-----";

  @Override
  public String getPublicKey() {
    return PUBLIC_KEY;
  }

  public String getPrivateKey(){
    return PRIVATE_KEY;
  }
}
