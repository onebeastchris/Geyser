package tel.schich.libdatachannel;

public class TrackInit {
    public static final TrackInit DEFAULT = new TrackInit(Track.Direction.DEFAULT, Track.Codec.DEFAULT);

    private final Track.Direction direction;
    private final Track.Codec codec;

    private TrackInit(Track.Direction direction, Track.Codec codec) {
        this.direction = direction;
        this.codec = codec;
    }

    public TrackInit withDirection(Track.Direction direction) {
        return new TrackInit(direction, codec);
    }

    public Track.Direction direction() {
        return direction;
    }

    public TrackInit withCodec(Track.Codec codec) {
        return new TrackInit(direction, codec);
    }

    public Track.Codec codec() {
        return codec;
    }
}
