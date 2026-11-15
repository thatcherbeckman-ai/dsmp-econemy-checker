package net.minecraft;
public class class_1297 {
 private int id; private double x,y,z; public boolean noGravity,glowing;
 public int field_6012;
 // Vanilla setGlowing does not change an existing client-side flag.
 public final void method_5834(boolean value){}
 protected void method_5729(int index,boolean value){if(index==6)glowing=value;} public double vx,vy,vz;
 public enum class_5529 { field_26999 }
 public int method_5628(){return id;} public void method_5838(int id){this.id=id;}
 public void method_5814(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
 public double method_23317(){return x;} public double method_23318(){return y;} public double method_23321(){return z;}
 public void method_5875(boolean noGravity){this.noGravity=noGravity;}
 public void method_18800(double x,double y,double z){vx=x;vy=y;vz=z;}
}
