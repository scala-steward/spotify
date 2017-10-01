package io.scarman.spotify.response

import io.scarman.spotify.util.SongDuration

case class Track(artists: List[Artist],
                 available_markets: List[String],
                 disc_number: Int,
                 duration_ms: Int,
                 explicit: Boolean,
                 external_urls: ExternalUrl,
                 href: String,
                 id: String,
                 name: String,
                 preview_url: String,
                 track_number: Int,
                 `type`: String,
                 uri: String,
                 is_playable: Option[Boolean],
                 linked_from: Option[TrackLink],
                 album: Option[SimpleAlbum],
                 popularity: Option[Int],
                 external_ids: Option[ExternalId])
    extends SongDuration