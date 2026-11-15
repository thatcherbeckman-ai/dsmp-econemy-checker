package dev.dsmp.economy.auction;

/** Feet position and dimension at Start, independent of subsequent player movement. */
public record AuctionAnchor(String dimension,double x,double y,double z) {
    public AuctionAnchor {
        if(dimension==null||dimension.length()>256||!dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||Math.abs(x)>32_000_000||Math.abs(y)>32_000_000||Math.abs(z)>32_000_000)throw new IllegalArgumentException("Invalid auction location");
    }
    public boolean nearby(String dimension,double px,double py,double pz){return this.dimension.equals(dimension)&&Double.isFinite(px)&&Double.isFinite(py)&&Double.isFinite(pz)&&Math.pow(x-px,2)+Math.pow(y-py,2)+Math.pow(z-pz,2)<=64*64;}
}
