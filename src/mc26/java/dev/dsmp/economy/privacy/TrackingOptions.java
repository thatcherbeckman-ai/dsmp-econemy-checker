package dev.dsmp.economy.privacy;

/** Independent opt-outs. No tracked records or appearance settings are stored here. */
public final class TrackingOptions {
    public boolean betRecordingOff=false,profitHidden=false,playerResultsHidden=false;
    public TrackingOptions copy(){var n=new TrackingOptions();n.betRecordingOff=betRecordingOff;n.profitHidden=profitHidden;n.playerResultsHidden=playerResultsHidden;return n;}
    public void turnOffAll(){betRecordingOff=profitHidden=playerResultsHidden=true;}
    public boolean recordsBets(boolean otherwiseEnabled){return otherwiseEnabled&&!betRecordingOff;}
    public boolean blocksModule(int id){return switch(id){case 0,4->playerResultsHidden;case 1->betRecordingOff||playerResultsHidden;case 2->profitHidden;default->false;};}
    public boolean[] visibleViews(boolean[] configured){if(configured==null||configured.length!=4)throw new IllegalArgumentException("Expected four views");var result=configured.clone();if(playerResultsHidden)result[0]=false;if(betRecordingOff||playerResultsHidden)result[1]=false;if(profitHidden)result[2]=false;return result;}
}
