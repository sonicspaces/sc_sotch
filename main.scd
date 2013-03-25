
m = Sotch();

//level check
s.meter;
//get ready
m.gui.addResponders;

//end the piece
m.end;
m.openScoreFile;

//test midi
MIDIClient.init;
MIDIIn.connectAll;
t = MIDIOut(0);
t.control(1,  51, 33);//que
t.control(1, 111, 127);//slider