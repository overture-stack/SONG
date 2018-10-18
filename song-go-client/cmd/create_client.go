package cmd

import (
	"github.com/overture-stack/SONG/song-go-client/song"
	"github.com/spf13/viper"
	"net/url"
)

func createClient() *song.Client {
	accessToken := viper.GetString("accessToken")
	songURL, err := url.Parse(viper.GetString("songURL"))
	if err != nil {
		panic(err)
	}
	client := song.CreateClient(accessToken, songURL)
	return client
}
