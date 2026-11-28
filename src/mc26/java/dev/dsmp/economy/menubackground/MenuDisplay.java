package dev.dsmp.economy.menubackground;

/** Small pure calculations shared by menu transparency and the decorative corner character. */
public final class MenuDisplay {
    public record AvatarBounds(int x,int y,int width,int height){public int right(){return x+width;}public int bottom(){return y+height;}}
    public record Look(float bodyYaw,float headYaw,float pitch){}
    private MenuDisplay(){}
    public static int alpha(int argb,int percent){int a=Math.round((argb>>>24)*Math.clamp(percent,0,100)/100f);return a<<24|argb&0xFFFFFF;}
    public static AvatarBounds avatar(int width,int height,double menuRight,double menuBottom,int percent){
        if(width<1||height<1)throw new IllegalArgumentException("Invalid viewport");
        double desired=Math.clamp(height*.14,42,110)*Math.clamp(percent,60,140)/100d;
        double rightSpace=Math.max(0,width-menuRight-8),bottomSpace=Math.max(0,height-menuBottom-8);
        double available=Math.max(rightSpace/.6,bottomSpace);int h=Math.max(1,(int)Math.floor(Math.min(desired,Math.min(Math.max(20,available),height-8))));
        int w=Math.max(1,Math.min(width,(int)Math.ceil(h*.6)));
        return new AvatarBounds(Math.max(0,width-w-4),Math.max(0,height-h-4),w,Math.min(h,height));
    }
    public static Look look(AvatarBounds b,double mouseX,double mouseY,boolean follows){
        if(!follows||!Double.isFinite(mouseX)||!Double.isFinite(mouseY))return new Look(180,0,-3);
        double yaw=Math.atan((b.x()+b.width()/2d-mouseX)/60)*30;
        double pitch=-Math.atan((b.y()+b.height()*.12-mouseY)/60)*20;
        return new Look((float)(180+yaw*.35),(float)(yaw*.65),(float)pitch);
    }
}
