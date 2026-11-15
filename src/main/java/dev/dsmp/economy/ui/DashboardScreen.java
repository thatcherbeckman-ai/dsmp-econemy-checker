package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.dashboard.*;
import dev.dsmp.economy.commands.LoanAction;
import dev.dsmp.economy.loans.LoanService;
import dev.dsmp.economy.core.Money;
import net.minecraft.*;
import java.util.*;

/** Responsive settings dashboard: read-only overview, explicit HUD controls and loan tools. */
public final class DashboardScreen extends class_437 {
    private static final String[] NAV={"Overview","HUD modules","Appearance","Text boxes","HUD layout","Loans","Tracking","Settings","Auctions"};
    private static final int WHITE=0xFFE7EAF3,MUTED=0xFF8993AC,GREEN=0xFF63D9A0,RED=0xFFE57885;
    private record Control(int x,int y,int width,int height,String label,boolean active,Runnable action){}
    private record LoanRow(String name,String value,boolean heading){}
    private final EconomyTrackerClient mod;
    private final DashboardSettings settings;
    private final List<Control> controls=new ArrayList<>();
    private int page,loanPage;
    private String notice="Changes apply live. Save or Cancel when finished.",nameDraft="",amountDraft="";
    private class_342 name,amount;
    public DashboardScreen(EconomyTrackerClient mod){super(class_2561.method_43470("Economy Dashboard"));this.mod=mod;settings=new DashboardSettings(mod.config);}
    public void checkpoint(){settings.checkpoint(mod.config);}
    private DashboardLayout layout(){return DashboardLayout.of(field_22789,field_22790);}
    @Override protected void method_25426(){
        controls.clear();name=null;amount=null;var d=layout();
        for(int i=0;i<NAV.length;i++){int target=i;button(8,d.navY(i),d.sidebar()-16,d.navStep()-2,NAV[i],page==i,()->navigate(target));}
        button(field_22789-108,16,44,20,"Save",true,this::method_25419);button(field_22789-60,16,52,20,"Cancel",false,this::cancel);
        if(page==1){int[] ids={5,2,0,1,4,3};for(int i=0;i<ids.length;i++){int id=ids[i],y=d.rowY(i)+(d.rowHeight()-18)/2;toggle(d,y,settings.visible(id),v->settings.visible(id,v));}}
        if(page==6){toggle(d,d.rowY(0)+5,mod.config.autoDetectPayments,v->mod.config.autoDetectPayments=v);toggle(d,d.rowY(1)+5,mod.config.scoreboardBalance,v->mod.config.scoreboardBalance=v);toggle(d,d.rowY(2)+5,mod.config.autoCycle,v->mod.config.autoCycle=v);button(field_22789-88,d.rowY(3)+4,34,20,"-5",false,()->change(()->settings.waitSeconds(-5)));button(field_22789-50,d.rowY(3)+4,34,20,"+5",false,()->change(()->settings.waitSeconds(5)));}
        if(page==7){toggle(d,d.rowY(0)+5,mod.config.festiveBackground,v->mod.config.festiveBackground=v);toggle(d,d.rowY(1)+5,mod.config.animateSnow,v->mod.config.animateSnow=v);button(field_22789-110,d.rowY(2)+4,94,20,"Cycle accent",true,()->change(settings::nextAccent));button(field_22789-88,d.rowY(3)+4,34,20,"-",false,()->change(()->settings.scale(-.05f)));button(field_22789-50,d.rowY(3)+4,34,20,"+",false,()->change(()->settings.scale(.05f)));button(field_22789-88,d.rowY(4)+4,34,20,"-20",false,()->change(()->settings.opacity(-20)));button(field_22789-50,d.rowY(4)+4,34,20,"+20",false,()->change(()->settings.opacity(20)));}
        if(page==5)initLoans(d);
    }
    private void button(int x,int y,int w,int h,String label,boolean active,Runnable action){controls.add(new Control(x,y,w,h,label,active,action));method_37063((class_364)class_4185.method_46430(class_2561.method_43470(label),b->action.run()).method_46434(x,y,w,h).method_46431());}
    private void toggle(DashboardLayout d,int y,boolean value,java.util.function.Consumer<Boolean> set){button(field_22789-84,y,32,18,"ON",value,()->change(()->set.accept(true)));button(field_22789-48,y,32,18,"OFF",!value,()->change(()->set.accept(false)));}
    private void change(Runnable action){action.run();method_41843();}
    private void navigate(int target){commitDraft();if(target==2){class_310.method_1551().method_1507(new AppearanceScreen(mod,this));return;}if(target==3){class_310.method_1551().method_1507(new TextBoxesScreen(mod,this));return;}if(target==8){class_310.method_1551().method_1507(new AuctionScreen(mod,this));return;}if(target==4){class_310.method_1551().method_1507(new TrackerScreen(mod));return;}page=target;notice=target==5?"Cancel reverts settings, not saved loans.":"Changes apply live. Save or Cancel when finished.";method_41843();}
    private void commitDraft(){if(name!=null){nameDraft=name.method_1882();amountDraft=amount.method_1882();}}
    private void initLoans(DashboardLayout d){
        int half=(d.contentWidth()-8)/2;
        name=new class_342(field_22793,d.left(),56,half,20,class_2561.method_43470("Player name"));name.method_1880(17);name.method_1852(nameDraft);name.method_1863(value->nameDraft=value);name.method_47404(class_2561.method_43470("Player name"));method_37063(name);
        amount=new class_342(field_22793,d.left()+half+8,56,half,20,class_2561.method_43470("Loan amount"));amount.method_1880(32);amount.method_1852(amountDraft);amount.method_1863(value->amountDraft=value);amount.method_47404(class_2561.method_43470("Amount, e.g. 10M"));method_37063(amount);
        String[] labels={"Set","Add","Repay","Remove","No loan +","No loan -"},operations={"set","add","paid","remove","noloan_add","noloan_remove"};int cw=(d.contentWidth()-12)/3;
        for(int i=0;i<6;i++){String op=operations[i];button(d.left()+(i%3)*(cw+6),82+(i/3)*24,cw,20,labels[i],i<3,()->loan(op));}
        button(d.left(),field_22790-48,48,20,"Prev",false,()->change(()->{commitDraft();loanPage=Math.max(0,loanPage-1);}));button(d.left()+54,field_22790-48,48,20,"Next",false,()->change(()->{commitDraft();loanPage=Math.min(loanPages(d)-1,loanPage+1);}));
    }
    private void loan(String operation){commitDraft();int result=((LoanAction)(Object)mod).loanCommand(operation,nameDraft.trim(),amountDraft.trim());notice=result==1?"Loan record saved.":"Could not update loan. See chat for details.";loanPage=0;method_41843();}
    private List<LoanRow> loanRows(){var data=LoanService.readOnlySnapshot();var rows=new ArrayList<LoanRow>();for(var loan:data.loans())rows.add(new LoanRow(loan.player(),Money.format(loan.cents(),false),false));if(!data.noLoans().isEmpty()){rows.add(new LoanRow("NO LOAN LIST","",true));for(String player:data.noLoans())rows.add(new LoanRow(player,"NO LOAN",false));}return rows;}
    private int loanPages(DashboardLayout d){return Math.max(1,(loanRows().size()+d.loanRows()-1)/d.loanRows());}
    @Override public void method_25394(class_332 g,int mx,int my,float delta){
        var d=layout();FestiveUi.background(g,field_22789,field_22790,mod.config);g.method_25294(0,0,d.sidebar(),field_22790,0xF50B0F1A);g.method_25294(d.sidebar()-1,0,d.sidebar(),field_22790,0xFF34304F);
        FestiveUi.text(g,"ECONOMY",10,19,WHITE,.9f,true);FestiveUi.text(g,mod.config.festiveBackground?"WINTER EDITION":"HUD CONTROL",10,35,0xFF000000|mod.config.menuAccent,.6f,false);
        FestiveUi.text(g,page==0?"Your economy":NAV[page],d.left(),18,WHITE,1.05f,true);
        String subtitle=switch(page){case 0->"Live overview and HUD controls";case 1->"Choose the HUD modules shown in game";case 5->"Loan edits save immediately";case 6->"Tracking on "+mod.config.serverAddress;case 7->"Menu theme and shared HUD defaults";default->"";};
        FestiveUi.text(g,FestiveUi.fit(subtitle,d.contentWidth(),.72f),d.left(),39,MUTED,.72f,false);
        if(page==0)overview(g,d);else if(page==1)modules(g,d);else if(page==5)loans(g,d);else if(page==6)tracking(g,d);else if(page==7)preferences(g,d);
        g.method_25294(d.left()-4,field_22790-23,field_22789,field_22790,0xE60B0F1A);
        String footer=notice;FestiveUi.text(g,FestiveUi.fit(footer,d.contentWidth(),.68f),d.left(),field_22790-16,MUTED,.68f,false);
        super.method_25394(g,mx,my,delta);
        for(var c:controls)FestiveUi.control(g,c.x,c.y,c.width,c.height,c.label,c.active,mx>=c.x&&mx<c.x+c.width&&my>=c.y&&my<c.y+c.height,mod.config.menuAccent);
    }
    private void row(class_332 g,DashboardLayout d,int index,String title,String detail){int y=d.rowY(index);FestiveUi.panel(g,d.left(),y,d.contentWidth(),d.rowHeight()-4,0x39304F);int room=d.contentWidth()-96;FestiveUi.text(g,FestiveUi.fit(title,room,.8f),d.left()+8,y+4,WHITE,.8f,true);if(d.rowHeight()>=25)FestiveUi.text(g,FestiveUi.fit(detail,room,.62f),d.left()+8,y+16,MUTED,.62f,false);}
    private void modules(class_332 g,DashboardLayout d){String[] names={"BALANCE","PROFIT TRACKER","PLAYER NETS","RECENT BETS","RAKEBACK","LOANS"};String[] descriptions={"Your name, money and change","Chart, winners and losses","Players up or down","Recent confirmed results","Net losses since reset","Outstanding loans and No Loan list"};for(int i=0;i<6;i++)row(g,d,i,names[i],descriptions[i]);}
    private void tracking(class_332 g,DashboardLayout d){row(g,d,0,"AUTOMATIC BETS","Capture named payment results");row(g,d,1,"BALANCE READER","Read visible balance and payment messages");row(g,d,2,"ROTATE HUD","Eight seconds between views, with fading");row(g,d,3,"RESULT WAIT: "+mod.config.paymentSettlementSeconds+"s","Time before an unpaid stake counts as a win");}
    private void preferences(class_332 g,DashboardLayout d){row(g,d,0,"CHRISTMAS BACKGROUND","Snowy trees, presents and colored lights");row(g,d,1,"SNOW ANIMATION","Gentle snowfall; off keeps a still scene");row(g,d,2,"MENU ACCENT","Personalize the dashboard highlight");row(g,d,3,"HUD BASE SIZE: "+Math.round(mod.config.scale*100)+"%","Base scale for the data panels");row(g,d,4,"GLASS OPACITY: "+mod.config.opacity,"Default opacity for panel styles");}
    private void loans(class_332 g,DashboardLayout d){
        var rows=loanRows();int count=d.loanRows();loanPage=Math.min(loanPage,loanPages(d)-1);FestiveUi.panel(g,d.left(),134,d.contentWidth(),Math.max(25,field_22790-192),0x5A457F);
        FestiveUi.text(g,"OUTSTANDING / NO LOAN",d.left()+7,138,MUTED,.65f,true);
        int from=loanPage*count;for(int i=0;i<count&&from+i<rows.size();i++){var row=rows.get(from+i);int y=152+i*15;FestiveUi.text(g,FestiveUi.fit(row.name,d.contentWidth()-88,.8f),d.left()+7,y,row.heading?0xFFC7A5E8:WHITE,.8f,row.heading);int color=row.value.equals("NO LOAN")?RED:GREEN;FestiveUi.text(g,row.value,field_22789-88,y,color,.75f,false);}
        if(rows.isEmpty())FestiveUi.text(g,"No loan records yet",d.left()+7,155,MUTED,.75f,false);
        FestiveUi.text(g,"Page "+(loanPage+1)+" / "+loanPages(d),d.left()+112,field_22790-42,MUTED,.75f,false);
    }
    private void overview(class_332 g,DashboardLayout d){
        int bannerHeight=WelcomeBanner.height(field_22790);WelcomeBanner.draw(g,d.left(),56,d.contentWidth(),bannerHeight,mod.config.menuAccent);int cardsTop=56+bannerHeight+8;
        int columns=d.contentWidth()>=400?4:2,cw=(d.contentWidth()-(columns-1)*8)/columns,ch=48;
        String balance=mod.ledger.balance().isPresent()?Money.format(mod.ledger.balance().getAsLong(),false):"--";
        String[] names={mod.wallet.estimated()?"BALANCE ~":"BALANCE","PROFIT","RECEIVED","PAID"};String[] values={balance,Money.format(mod.ledger.displayedProfit(),true),Money.format(mod.ledger.displayedMoneyIn(),false),Money.format(mod.ledger.displayedMoneyOut(),false)};
        int[] colors={0xFFD8BD6A,mod.ledger.displayedProfit()<0?RED:GREEN,0xFF9680E8,0xFF69B5CF};
        for(int i=0;i<4;i++){int x=d.left()+(i%columns)*(cw+8),y=cardsTop+(i/columns)*(ch+8);FestiveUi.panel(g,x,y,cw,ch,colors[i]&0xFFFFFF);FestiveUi.text(g,names[i],x+7,y+7,MUTED,.63f,true);FestiveUi.text(g,FestiveUi.fit(values[i],cw-12,1.15f),x+7,y+21,colors[i],1.15f,true);}
        int graphTop=cardsTop+((4+columns-1)/columns)*(ch+8);
        if(d.contentWidth()>=450){
            int feedWidth=Math.min(280,d.contentWidth()/3),sectionHeight=Math.min(280,Math.max(54,(field_22790-graphTop-40)/2));
            feed(g,d.left(),graphTop,feedWidth,sectionHeight);graph(g,d.left()+feedWidth+8,graphTop,d.contentWidth()-feedWidth-8,sectionHeight);
            int boardTop=graphTop+sectionHeight+10,boardHeight=Math.min(160,field_22790-boardTop-28);
            if(boardHeight>=50){var players=mod.ledger.playerNets();int bw=(d.contentWidth()-8)/2;board(g,d.left(),boardTop,bw,boardHeight,"TOP WINNERS",players.stream().filter(v->v.cents()>0).limit(5).toList(),GREEN);board(g,d.left()+bw+8,boardTop,bw,boardHeight,"TOP LOSERS",players.stream().filter(v->v.cents()<0).sorted(java.util.Comparator.comparingLong(dev.dsmp.economy.core.Ledger.PlayerNet::cents)).limit(5).toList(),RED);}
            return;
        }
        int graphHeight=Math.min(164,field_22790-graphTop-30);
        if(graphHeight>=36)graph(g,d.left(),graphTop,d.contentWidth(),graphHeight);
        int feedTop=graphTop+graphHeight+10,feedHeight=field_22790-feedTop-28;
        if(feedHeight>=42){FestiveUi.panel(g,d.left(),feedTop,d.contentWidth(),feedHeight,0x564578);FestiveUi.text(g,"RECENT BETS",d.left()+7,feedTop+7,MUTED,.7f,true);int count=Math.max(0,(feedHeight-25)/16);var bets=mod.ledger.recentBets();for(int i=0;i<Math.min(count,bets.size());i++){var bet=bets.get(i);int y=feedTop+23+i*16;FestiveUi.text(g,FestiveUi.fit(bet.player(),d.contentWidth()-90,.8f),d.left()+8,y,WHITE,.8f,false);FestiveUi.text(g,Money.format(-bet.playerDelta(),true),field_22789-90,y,bet.playerDelta()>0?RED:GREEN,.8f,true);}if(bets.isEmpty())FestiveUi.text(g,"No recorded bets yet",d.left()+8,feedTop+23,MUTED,.75f,false);}
    }
    private void feed(class_332 g,int x,int y,int w,int h){
        FestiveUi.panel(g,x,y,w,h,0x564578);FestiveUi.text(g,"RECENT BETS",x+7,y+7,MUTED,.7f,true);var bets=mod.ledger.recentBets();int count=Math.min(bets.size(),Math.max(0,(h-25)/18));
        for(int i=0;i<count;i++){var b=bets.get(i);int row=y+25+i*18;FestiveUi.text(g,FestiveUi.fit(b.player(),w-75,.75f),x+7,row,WHITE,.75f,false);FestiveUi.text(g,Money.format(-b.playerDelta(),true),x+w-70,row,b.playerDelta()>0?RED:GREEN,.75f,true);}
        if(bets.isEmpty())FestiveUi.text(g,FestiveUi.fit("No recorded bets yet",w-14,.75f),x+7,y+26,MUTED,.75f,false);
    }
    private void board(class_332 g,int x,int y,int w,int h,String title,java.util.List<dev.dsmp.economy.core.Ledger.PlayerNet> players,int color){
        FestiveUi.panel(g,x,y,w,h,color&0xFFFFFF);FestiveUi.text(g,title,x+7,y+7,color,.7f,true);int count=Math.min(players.size(),Math.max(0,(h-25)/18));
        for(int i=0;i<count;i++){var p=players.get(i);FestiveUi.text(g,(i+1)+"  "+FestiveUi.fit(p.player(),w-100,.8f),x+7,y+25+i*18,WHITE,.8f,false);FestiveUi.text(g,Money.format(p.cents(),true),x+w-80,y+25+i*18,color,.8f,true);}
        if(players.isEmpty())FestiveUi.text(g,"No results yet",x+7,y+26,MUTED,.75f,false);
    }
    private void graph(class_332 g,int x,int y,int w,int h){
        FestiveUi.panel(g,x,y,w,h,0x564578);FestiveUi.text(g,"BALANCE HISTORY",x+7,y+7,MUTED,.7f,true);var plot=BalanceCurve.of(mod.ledger.balances());
        if(plot.samples().isEmpty()){FestiveUi.text(g,FestiveUi.fit("No balance readings yet",w-14,.8f),x+7,y+23,MUTED,.8f,false);return;}
        int left=x+8,right=x+w-8,top=y+25,bottom=y+h-8;for(int i=1;i<4;i++){int gy=top+(bottom-top)*i/4;g.method_25294(left,gy,right,gy+1,0x263E4A65);}
        int color=plot.samples().getLast().cents()<plot.samples().getFirst().cents()?RED:GREEN;
        int previous=-1;
        for(int px=0;px<Math.max(1,right-left);px++){int index=(int)Math.round(px/(double)Math.max(1,right-left-1)*(plot.samples().size()-1));double value=plot.samples().get(index).cents(),fraction=plot.max()==plot.min()?.5:(value-plot.min())/(plot.max()-plot.min());int gy=bottom-(int)Math.round(fraction*Math.max(1,bottom-top));if(plot.samples().size()==1){int cx=(left+right)/2;g.method_25294(cx-1,gy-1,cx+2,gy+2,color);break;}g.method_25294(left+px,gy,left+px+1,bottom,0x32000000|color&0xFFFFFF);g.method_25294(left+px,previous<0?gy:Math.min(previous,gy),left+px+1,(previous<0?gy:Math.max(previous,gy))+1,color);previous=gy;}
    }
    @Override public void method_25419(){commitDraft();if(mod.saveConfig())class_310.method_1551().method_1507(null);else notice="Could not save. Retry Save or use Cancel.";}
    private void cancel(){mod.config=settings.cancel();class_310.method_1551().method_1507(null);}
    @Override public boolean method_25421(){return false;}
}
