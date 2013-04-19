
m = Sotch();
//get ready
m.gui;
//end the piece
m.end;

//test midi
t = MIDIOut(0);
t.control(1,  51, 33);//que
t.control(1, 111, 127);//slider

m.openScoreFile;