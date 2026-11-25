package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.dashboard.*;
import dev.dsmp.economy.commands.LoanAction;
import dev.dsmp.economy.loans.LoanService;
import dev.dsmp.economy.core.Money;

import java.util.*;

/** Responsive settings dashboard: read-only overview, explicit HUD controls and loan tools. */
public final class DashboardScreen extends net.minecraft.client.gui.screens.Screen {
    private static final String[] NAV={"Overview","HUD modules","Appearance","Text boxes","HUD layout","Loans","Tracking","Settings","Auctions","Inventory themes"};
    private static final int WHITE=0xFFE7EAF3,MUTED=0xFF8993AC,GREEN=0xFF63D9A0,RED=0xFFE57885;
    private record LoanRow(String name,String value,boolean heading){}
    private final EconomyTrackerClient mod;
    private final DashboardSettings settings;
    private int page,loanPage;
    private String notice="Changes apply live. Save or Cancel when finished.",nameDraft="",amountDraft="";
    private net.minecraft.client.gui.components.EditBox name,amount;
    public DashboardScreen(EconomyTrackerClient mod){super(net.minecraft.network.chat.Component.literal(MenuLayout.BRAND));this.mod=mod;settings=new DashboardSettings(mod.config);}
    public void checkpoint(){settings.checkpoint(mod.config);}
    private MenuLayout.Dashboard layout(){return MenuLayout.Dashboard.of(width,height);}
    @Override protected void init(){
        name=null;amount=null;var d=layout();
        for(int i=0;i<NAV.length;i++){int target=i;addRenderableWidget(new SettingsButton(10,d.navY(i),d.sidebar()-20,d.navHeight(),NAV[i],page==i,mod.config.menuAccent,()->navigate(target)).navigation());}
        var menu=MenuLayout.of(width,height);button(menu.doneX(),menu.actionY(),menu.actionWidth(),menu.buttonHeight(),"Save",true,this::onClose);button(menu.cancelX(),menu.actionY(),menu.actionWidth(),menu.buttonHeight(),"Cancel",false,this::cancel);
        if(page==1){int[] ids={5,2,0,1,4,3};for(int i=0;i<ids.length;i++){int id=ids[i],y=d.controlY(i);toggle(d,y,settings.visible(id),v->settings.visible(id,v));}}
        if(page==6){toggle(d,d.controlY(0),mod.config.autoDetectPayments,v->mod.config.autoDetectPayments=v);toggle(d,d.controlY(1),mod.config.scoreboardBalance,v->mod.config.scoreboardBalance=v);toggle(d,d.controlY(2),mod.config.autoCycle,v->mod.config.autoCycle=v);button(width-88,d.controlY(3),34,d.controlHeight(),"-5",false,()->change(()->settings.waitSeconds(-5)));button(width-50,d.controlY(3),34,d.controlHeight(),"+5",false,()->change(()->settings.waitSeconds(5)));}
        if(page==7){button(width-110,d.controlY(0),94,d.controlHeight(),"Backgrounds",true,()->net.minecraft.client.Minecraft.getInstance().gui.setScreen(new MenuBackgroundScreen(mod,this)));toggle(d,d.controlY(1),mod.config.festiveBackground,v->mod.config.festiveBackground=v);toggle(d,d.controlY(2),mod.config.animateSnow,v->mod.config.animateSnow=v);button(width-110,d.controlY(3),94,d.controlHeight(),"Cycle accent",true,()->change(settings::nextAccent));button(width-88,d.controlY(4),34,d.controlHeight(),"-",false,()->change(()->settings.scale(-.05f)));button(width-50,d.controlY(4),34,d.controlHeight(),"+",false,()->change(()->settings.scale(.05f)));button(width-88,d.controlY(5),34,d.controlHeight(),"-20",false,()->change(()->settings.opacity(-20)));button(width-50,d.controlY(5),34,d.controlHeight(),"+20",false,()->change(()->settings.opacity(20)));}
        if(page==5)initLoans(d);
    }
    private void button(int x,int y,int w,int h,String label,boolean active,Runnable action){addRenderableWidget(new SettingsButton(x,y,w,h,label,active,mod.config.menuAccent,action));}
    private void toggle(MenuLayout.Dashboard d,int y,boolean value,java.util.function.Consumer<Boolean> set){button(width-108,y,40,d.controlHeight(),"ON",value,()->change(()->set.accept(true)));button(width-58,y,40,d.controlHeight(),"OFF",!value,()->change(()->set.accept(false)));}
    private void change(Runnable action){action.run();rebuildWidgets();}
    private void navigate(int target){commitDraft();if(target==9){net.minecraft.client.Minecraft.getInstance().gui.setScreen(new InventoryThemesScreen(mod,this));return;}if(target==2){net.minecraft.client.Minecraft.getInstance().gui.setScreen(new AppearanceScreen(mod,this));return;}if(target==3){net.minecraft.client.Minecraft.getInstance().gui.setScreen(new TextBoxesScreen(mod,this));return;}if(target==8){net.minecraft.client.Minecraft.getInstance().gui.setScreen(new AuctionScreen(mod,this));return;}if(target==4){net.minecraft.client.Minecraft.getInstance().gui.setScreen(new TrackerScreen(mod));return;}page=target;notice=target==5?"Cancel reverts settings, not saved loans.":"Changes apply live. Save or Cancel when finished.";rebuildWidgets();}
    private void commitDraft(){if(name!=null){nameDraft=name.getValue();amountDraft=amount.getValue();}}
    private void initLoans(MenuLayout.Dashboard d){
        int gap=d.gap(),h=d.controlHeight(),half=(d.contentWidth()-gap)/2,y=d.bodyTop();
        name=new SettingsEditBox(font,d.left(),y,half,h,net.minecraft.network.chat.Component.literal("Player name"));name.setMaxLength(17);name.setValue(nameDraft);name.setResponder(value->nameDraft=value);name.setHint(net.minecraft.network.chat.Component.literal("Player name"));addRenderableWidget(name);
        amount=new SettingsEditBox(font,d.left()+half+gap,y,half,h,net.minecraft.network.chat.Component.literal("Loan amount"));amount.setMaxLength(32);amount.setValue(amountDraft);amount.setResponder(value->amountDraft=value);amount.setHint(net.minecraft.network.chat.Component.literal("Amount, e.g. 10M"));addRenderableWidget(amount);
        String[] labels={"Set","Add","Repay","Remove","No loan +","No loan -"},operations={"set","add","paid","remove","noloan_add","noloan_remove"};int cw=(d.contentWidth()-gap*2)/3;
        for(int i=0;i<6;i++){String op=operations[i];button(d.left()+(i%3)*(cw+gap),y+(i/3+1)*(h+gap),cw,h,labels[i],i<3,()->loan(op));}
        button(d.left(),height-52,58,h,"Prev",false,()->change(()->{commitDraft();loanPage=Math.max(0,loanPage-1);}));button(d.left()+58+gap,height-52,58,h,"Next",false,()->change(()->{commitDraft();loanPage=Math.min(loanPages(d)-1,loanPage+1);}));
    }

    private void loan(String operation){commitDraft();int result=((LoanAction)(Object)mod).loanCommand(operation,nameDraft.trim(),amountDraft.trim());notice=result==1?"Loan record saved.":"Could not update loan. See chat for details.";loanPage=0;rebuildWidgets();}
    private List<LoanRow> loanRows(){var data=LoanService.readOnlySnapshot();var rows=new ArrayList<LoanRow>();for(var loan:data.loans())rows.add(new LoanRow(loan.player(),Money.format(loan.cents(),false),false));if(!data.noLoans().isEmpty()){rows.add(new LoanRow("NO LOAN LIST","",true));for(String player:data.noLoans())rows.add(new LoanRow(player,"NO LOAN",false));}return rows;}
    private int loanPages(MenuLayout.Dashboard d){return Math.max(1,(loanRows().size()+d.loanRows()-1)/d.loanRows());}
    @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g,int mx,int my,float delta){
        var d=layout();FestiveUi.background(g,width,height,mod.config);g.fill(0,0,d.sidebar(),height,0xF30B1220);g.fill(d.sidebar()-1,0,d.sidebar(),height,0x454D5C77);
        int brandX=d.sidebar()>=130?48:10,brandRoom=d.sidebar()-brandX-10;
        if(d.sidebar()>=130){FestiveUi.surface(g,12,18,27,28,0xFF222C43);FestiveUi.text(g,"T",21,23,0xFFDCCFFF,1.1f,true);}
        FestiveUi.text(g,"thatchacats",brandX,20,WHITE,Math.min(.95f,brandRoom/(float)MenuText.width("thatchacats",true)),true);FestiveUi.text(g,"econemy checker",brandX,37,0xFFB3A6D4,Math.min(.67f,brandRoom/(float)MenuText.width("econemy checker")),false);
        String heading=page==0?"Overview":NAV[page];int headingRoom=MenuLayout.of(width,height).doneX()-d.left()-10;FestiveUi.text(g,heading,d.left(),18,WHITE,Math.min(1.05f,headingRoom/(float)Math.max(1,MenuText.width(heading,true))),true);
        String subtitle=switch(page){case 0->"Live overview and HUD controls";case 1->"Choose the HUD modules shown in game";case 5->"Loan edits save immediately";case 6->"Tracking on "+mod.config.serverAddress;case 7->"Menu theme and shared HUD defaults";default->"";};
        FestiveUi.text(g,FestiveUi.fit(subtitle,d.contentWidth(),.72f),d.left(),height>=480?48:42,MUTED,.72f,false);
        if(page==0)overview(g,d);else if(page==1)modules(g,d);else if(page==5)loans(g,d);else if(page==6)tracking(g,d);else if(page==7)preferences(g,d);
        FestiveUi.surface(g,d.left()-4,height-24,width-d.left()-6,20,0xDD0D1421);
        String footer=notice;FestiveUi.text(g,FestiveUi.fit(footer,d.contentWidth(),.68f),d.left(),height-16,MUTED,.68f,false);
        super.extractRenderState(g,mx,my,delta);
    }
    private void row(net.minecraft.client.gui.GuiGraphicsExtractor g,MenuLayout.Dashboard d,int index,String title,String detail){int y=d.rowY(index);FestiveUi.panel(g,d.left(),y,d.contentWidth(),d.rowHeight()-d.gap(),0x39304F);int room=d.contentWidth()-126;FestiveUi.text(g,FestiveUi.fit(title,room,.8f),d.left()+12,y+7,WHITE,.8f,true);if(d.rowHeight()>=38)FestiveUi.text(g,FestiveUi.fit(detail,room,.62f),d.left()+12,y+24,MUTED,.62f,false);}
    private void modules(net.minecraft.client.gui.GuiGraphicsExtractor g,MenuLayout.Dashboard d){String[] names={"BALANCE","PROFIT TRACKER","PLAYER NETS","RECENT BETS","RAKEBACK","LOANS"};String[] descriptions={"Your name, money and change","Chart, winners and losses","Players up or down","Recent confirmed results","Net losses since reset","Outstanding loans and No Loan list"};for(int i=0;i<6;i++)row(g,d,i,names[i],descriptions[i]);}
    private void tracking(net.minecraft.client.gui.GuiGraphicsExtractor g,MenuLayout.Dashboard d){row(g,d,0,"AUTOMATIC BETS","Capture named payment results");row(g,d,1,"BALANCE READER","Read visible balance and payment messages");row(g,d,2,"ROTATE HUD","Eight seconds between views, with fading");row(g,d,3,"RESULT WAIT: "+mod.config.paymentSettlementSeconds+"s","Time before an unpaid stake counts as a win");}
    private void preferences(net.minecraft.client.gui.GuiGraphicsExtractor g,MenuLayout.Dashboard d){row(g,d,0,"MENU BACKGROUND","Scenery, imported images, blur and particles");row(g,d,1,"CHRISTMAS BACKGROUND","Decorations in the Original background");row(g,d,2,"SNOW ANIMATION","Animation in the Original background");row(g,d,3,"MENU ACCENT","Personalize the dashboard highlight");row(g,d,4,"HUD BASE SIZE: "+Math.round(mod.config.scale*100)+"%","Base scale for the data panels");row(g,d,5,"GLASS OPACITY: "+mod.config.opacity,"Default opacity for panel styles");}
    private void loans(net.minecraft.client.gui.GuiGraphicsExtractor g,MenuLayout.Dashboard d){
        var rows=loanRows();int count=d.loanRows();loanPage=Math.min(loanPage,loanPages(d)-1);FestiveUi.panel(g,d.left(),d.loanTop(),d.contentWidth(),Math.max(25,height-d.loanTop()-62),0x5A457F);
        FestiveUi.text(g,"OUTSTANDING / NO LOAN",d.left()+10,d.loanTop()+8,MUTED,.65f,true);
        int from=loanPage*count;for(int i=0;i<count&&from+i<rows.size();i++){var row=rows.get(from+i);int y=d.loanTop()+26+i*18;FestiveUi.text(g,FestiveUi.fit(row.name,d.contentWidth()-88,.8f),d.left()+7,y,row.heading?0xFFC7A5E8:WHITE,.8f,row.heading);int color=row.value.equals("NO LOAN")?RED:GREEN;FestiveUi.text(g,row.value,width-88,y,color,.75f,false);}
        if(rows.isEmpty())FestiveUi.text(g,"No loan records yet",d.left()+10,d.loanTop()+26,MUTED,.75f,false);
        FestiveUi.text(g,"Page "+(loanPage+1)+" / "+loanPages(d),d.left()+132,height-44,MUTED,.75f,false);
    }
    private void overview(net.minecraft.client.gui.GuiGraphicsExtractor g,MenuLayout.Dashboard d){
        int bannerHeight=WelcomeBanner.height(height);WelcomeBanner.draw(g,d.left(),d.bodyTop(),d.contentWidth(),bannerHeight,mod.config.menuAccent);int cardsTop=d.bodyTop()+bannerHeight+12;
        int columns=d.contentWidth()>=400?4:2,cw=(d.contentWidth()-(columns-1)*8)/columns,ch=48;
        String balance=mod.ledger.balance().isPresent()?Money.format(mod.ledger.balance().getAsLong(),false):"--";
        String[] names={mod.wallet.estimated()?"BALANCE ~":"BALANCE","PROFIT","RECEIVED","PAID"};String[] values={balance,Money.format(mod.ledger.displayedProfit(),true),Money.format(mod.ledger.displayedMoneyIn(),false),Money.format(mod.ledger.displayedMoneyOut(),false)};
        int[] colors={0xFFD8BD6A,mod.ledger.displayedProfit()<0?RED:GREEN,0xFF9680E8,0xFF69B5CF};
        for(int i=0;i<4;i++){int x=d.left()+(i%columns)*(cw+8),y=cardsTop+(i/columns)*(ch+8);FestiveUi.panel(g,x,y,cw,ch,colors[i]&0xFFFFFF);FestiveUi.text(g,names[i],x+7,y+7,MUTED,.63f,true);FestiveUi.text(g,FestiveUi.fit(values[i],cw-12,1.15f),x+7,y+21,colors[i],1.15f,true);}
        int graphTop=cardsTop+((4+columns-1)/columns)*(ch+8);
        if(d.contentWidth()>=450){
            int feedWidth=Math.min(280,d.contentWidth()/3),sectionHeight=Math.min(280,Math.max(54,(height-graphTop-40)/2));
            feed(g,d.left(),graphTop,feedWidth,sectionHeight);graph(g,d.left()+feedWidth+8,graphTop,d.contentWidth()-feedWidth-8,sectionHeight);
            int boardTop=graphTop+sectionHeight+10,boardHeight=Math.min(160,height-boardTop-28);
            if(boardHeight>=50){var players=mod.ledger.playerNets();int bw=(d.contentWidth()-8)/2;board(g,d.left(),boardTop,bw,boardHeight,"TOP WINNERS",players.stream().filter(v->v.cents()>0).limit(5).toList(),GREEN);board(g,d.left()+bw+8,boardTop,bw,boardHeight,"TOP LOSERS",players.stream().filter(v->v.cents()<0).sorted(java.util.Comparator.comparingLong(dev.dsmp.economy.core.Ledger.PlayerNet::cents)).limit(5).toList(),RED);}
            return;
        }
        int graphHeight=Math.min(164,height-graphTop-30);
        if(graphHeight>=36)graph(g,d.left(),graphTop,d.contentWidth(),graphHeight);
        int feedTop=graphTop+graphHeight+10,feedHeight=height-feedTop-28;
        if(feedHeight>=42){FestiveUi.panel(g,d.left(),feedTop,d.contentWidth(),feedHeight,0x564578);FestiveUi.text(g,"RECENT BETS",d.left()+7,feedTop+7,MUTED,.7f,true);int count=Math.max(0,(feedHeight-25)/16);var bets=mod.ledger.recentBets();for(int i=0;i<Math.min(count,bets.size());i++){var bet=bets.get(i);int y=feedTop+23+i*16;FestiveUi.text(g,FestiveUi.fit(bet.player(),d.contentWidth()-90,.8f),d.left()+8,y,WHITE,.8f,false);FestiveUi.text(g,Money.format(-bet.playerDelta(),true),width-90,y,bet.playerDelta()>0?RED:GREEN,.8f,true);}if(bets.isEmpty())FestiveUi.text(g,"No recorded bets yet",d.left()+8,feedTop+23,MUTED,.75f,false);}
    }
    private void feed(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,int w,int h){
        FestiveUi.panel(g,x,y,w,h,0x564578);FestiveUi.text(g,"RECENT BETS",x+7,y+7,MUTED,.7f,true);var bets=mod.ledger.recentBets();int count=Math.min(bets.size(),Math.max(0,(h-25)/18));
        for(int i=0;i<count;i++){var b=bets.get(i);int row=y+25+i*18;FestiveUi.text(g,FestiveUi.fit(b.player(),w-75,.75f),x+7,row,WHITE,.75f,false);FestiveUi.text(g,Money.format(-b.playerDelta(),true),x+w-70,row,b.playerDelta()>0?RED:GREEN,.75f,true);}
        if(bets.isEmpty())FestiveUi.text(g,FestiveUi.fit("No recorded bets yet",w-14,.75f),x+7,y+26,MUTED,.75f,false);
    }
    private void board(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,int w,int h,String title,java.util.List<dev.dsmp.economy.core.Ledger.PlayerNet> players,int color){
        FestiveUi.panel(g,x,y,w,h,color&0xFFFFFF);FestiveUi.text(g,title,x+7,y+7,color,.7f,true);int count=Math.min(players.size(),Math.max(0,(h-25)/18));
        for(int i=0;i<count;i++){var p=players.get(i);FestiveUi.text(g,(i+1)+"  "+FestiveUi.fit(p.player(),w-100,.8f),x+7,y+25+i*18,WHITE,.8f,false);FestiveUi.text(g,Money.format(p.cents(),true),x+w-80,y+25+i*18,color,.8f,true);}
        if(players.isEmpty())FestiveUi.text(g,"No results yet",x+7,y+26,MUTED,.75f,false);
    }
    private void graph(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,int w,int h){
        FestiveUi.panel(g,x,y,w,h,0x564578);FestiveUi.text(g,"BALANCE HISTORY",x+7,y+7,MUTED,.7f,true);var plot=BalanceCurve.of(mod.ledger.balances());
        if(plot.samples().isEmpty()){FestiveUi.text(g,FestiveUi.fit("No balance readings yet",w-14,.8f),x+7,y+23,MUTED,.8f,false);return;}
        int left=x+8,right=x+w-8,top=y+25,bottom=y+h-8;for(int i=1;i<4;i++){int gy=top+(bottom-top)*i/4;g.fill(left,gy,right,gy+1,0x263E4A65);}
        int color=plot.samples().getLast().cents()<plot.samples().getFirst().cents()?RED:GREEN;
        int previous=-1;
        for(int px=0;px<Math.max(1,right-left);px++){int index=(int)Math.round(px/(double)Math.max(1,right-left-1)*(plot.samples().size()-1));double value=plot.samples().get(index).cents(),fraction=plot.max()==plot.min()?.5:(value-plot.min())/(plot.max()-plot.min());int gy=bottom-(int)Math.round(fraction*Math.max(1,bottom-top));if(plot.samples().size()==1){int cx=(left+right)/2;g.fill(cx-1,gy-1,cx+2,gy+2,color);break;}g.fill(left+px,gy,left+px+1,bottom,0x32000000|color&0xFFFFFF);g.fill(left+px,previous<0?gy:Math.min(previous,gy),left+px+1,(previous<0?gy:Math.max(previous,gy))+1,color);previous=gy;}
    }
    @Override public void onClose(){commitDraft();if(mod.saveConfig())net.minecraft.client.Minecraft.getInstance().gui.setScreen(null);else notice="Could not save. Retry Save or use Cancel.";}
    private void cancel(){mod.config=settings.cancel();net.minecraft.client.Minecraft.getInstance().gui.setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
}
