//0
"initialize"
//1
~fz = Synth(\freeze0, [fade: 1, freq: 50, amp: 0.2, wipe: 1]);
//2
~fz.release(0.1);
//3
~fz = Synth(\freeze0, [fade: 4, freq: 1.5, amp: 0.3, wipe: 0.1]);
//4
~ca0 = Synth(\ca0, [fade: 0, buf: m.bufs[5]]);
//5
~ca0.release(0.1);~ca1 = Synth(\ca1, [fade: 0, buf: m.bufs[6]]);
//6
~ca1.release(0.01);
~fz.release(0.1);
//7
~cut = BBCut2(
		CutStream1(),
		WarpCutProc1(
			{ [1.3, 2.6, 5.2] wchoose: [0.7, 0.2, 0.1] },
			{ [9, 19, 35].choose },
			[0.0, 0.5, 0.5],
			{ 100 + 100.rand },
			{ 0.8 + 0.15.rand }
		)
	);
~cut.play(1.8).amp_(1);
//8
~cut.pause;
//9
~cut.resume;
//10
~ca0 = Synth(\ca0, [fade: 0, buf: m.bufs[5]]);
//11
~ca0.release(0.1);~ca1 = Synth(\ca1, [fade: 0, buf: m.bufs[6]]);
~cut.pause;
//12
~ca1.release(0.01);
//13
~cut.end;
~cut = BBCut2(
		CutStream1(),
		WarpCutProc1(
			{ [1, 2, 4] wchoose: [0.7, 0.2, 0.1] },
			{ [9, 19, 29].choose },
			[0.0, 0.5, 0.5],
			{ 100 + 100.rand },
			{ 0.8 + 0.15.rand }
		)
	);
~cut.play(2.2);
~ca0 = Synth(\ca0, [fade: 0, buf: m.bufs[7]]);
//14
~ca0.release(0.01);
Routine({ 1.wait; ~cut.pause; }).play;
//15
~ca0 = Synth(\ca0, [fade: 0, buf: m.bufs[8]]);
//16
m;
//17
~ca0.release(0.01);
//18
~cut.resume;
//19
~pb = Synth(\play, [buf: m.bufs[0], amp: 0.8]);~cut.pause;
//20
~cut.resume;
//21
~cut.pause; ~pb = Synth(\play, [buf: m.bufs[1], rate: 0.7, amp: 0.5]);
//22
~pb.release;~cut.resume;
//23
~cut.pause;
//24
~pb = Synth(\play, [buf: m.bufs[2], fade: 3, amp: 0.5])
//25
Routine({ 1.1.wait; ~pb.release(0.03) }).play;
//26
~cut.resume;
//27
~cut.pause;~pb = Synth(\play, [buf: m.bufs[3], amp: 0.8]);
//28
~pb.release(0.01);
//29
~cut.resume;
//30
~cut.pause;
//31
~cut.resume;
//32
~cut.end;
//33
/* ___________________ mv 2 ___________________ */
~lg0 = Synth(\lgst0, [fade: 10, amp: 0.05]);
//34
~lg0.release(40);
~lg1 = Synth(\lgst1, [fade: 40, amp: 0.6]);
Routine({
	15.wait;
	~cm0 = Synth(\cnfr0, [fade: 5, amp: 0.5]);
	NodeWatcher.register(~cm0);
}).play;
//35
~cut = BBCut2(
		CutStream1(s.options.numOutputBusChannels + 1),
		WarpCutProc1(
			{ [1.3, 2.6, 5.2] wchoose: [0.7, 0.2, 0.1] },
			{ [9, 19, 35].choose },
			[0.0, 0.5, 0.5],
			{ 100 + 100.rand },
			{ 0.8 + 0.15.rand }
		)
);
~cut.play(1.8).amp_(1);
//36
~cm0.release;
~lg1.release(20);
~cut.end;
//37
/* ___________________ mv 3 ___________________ */
~ac = Synth(\comb0, [fade: 1, amp: 0.1]);
//38
~ac.set(\freq, 5.0);
//39
~ca1 = Synth(\ca1, [fade: 0, buf: m.bufs[6]]);
~cut = BBCut2(
		CutStream1(),
		WarpCutProc1(
			{ [1,2,4] wchoose: [0.7,0.2,0.1] },
			{ [9,19,29].choose },
			[0.0, 0.5, 0.5],
			{ 100 + 100.rand },
			{ 0.8 + 0.15.rand }
		);
);
~cut.play(2.2);
~ac.release;
//40
~ca1.release(0.01);
~ca0 = Synth(\ca0, [fade: 0, buf: m.bufs[8], amp: 0.3]);
//41
~ca0.release(0.01);
~cut.pause;
//42
~cut = BBCut2(
		CutStream1(),
		WarpCutProc1(
			{ [1, 2, 4] wchoose: [0.7, 0.2, 0.1] },
			{ [9, 19, 29].choose },
			[0.0, 0.5, 0.5],
			{ 100 + 100.rand },
			{ 0.8 + 0.15.rand }
	)
);
~cut.play(0.5);
Routine({
	2.wait;
	~pb = Synth(\play, [buf: m.bufs[0], fade: 0.5, amp: 0.2, rate: 0.25]);
	5.wait;
	~pb.release(0.1);
}).play;
//43
~cut.end;
~cut = BBCut2(
		CutStream1(),
		WarpCutProc1(
			{ [1, 2, 4] wchoose: [0.7, 0.2, 0.1] },
			{ [9, 19, 29].choose },
			[0.0, 0.5, 0.5],
			{ 100 + 100.rand },
			{ 0.8 + 0.15.rand }
		)
);
~cut.play(2.2);
~pb = Synth(\play, [buf: m.bufs[3], amp: 0.5, rate: 0.3]);
//44
~pb.release(0.01);~cut.pause;
//45
~pb = Synth(\play, [buf: m.bufs[3], amp: 0.5, rate: 0.3]);
//46
~pb.release(0.05);~cut.resume;
//47
~pb = Synth(\play, [buf: m.bufs[1], amp: 0.25, rate: 0.3]);
~cut.pause;
//48
~pb.release(0.01);
//49
~pb = Synth(\play, [buf: m.bufs[1], amp: 0.15, rate: 0.235]);
//50
~pb.release(0.1);
//51
~pb = Synth(\play, [buf: m.bufs[0], amp: 0.07, rate: 1.0]);
//52