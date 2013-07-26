//for sotch
//sums up 2 inputs together

CutStream0 : CutSynth {

	var <>inbus,<>bbcutbuf, deallocate;
	var dutycycle, atkprop, relprop, curve;
	var playdef, recdef;

	*new { arg inbus, bbcutbuf, dutycycle, atkprop, relprop, curve;
		^super.new.initCutStream0(inbus, bbcutbuf, dutycycle, atkprop, relprop, curve);
	}

	initCutStream0 { arg ib, bcb, dc, ap, rp, c;
		deallocate = false;
		inbus      = ib ?? { Server.default.options.numOutputBusChannels };
		bbcutbuf   = bcb; //could be nil, dealt with in setup
		dutycycle  = dc ? 1.0;
		atkprop    = ap ? 0.001;
		relprop    = rp ? 0.001;
		curve      = c ? 0;
	}

	setup {
		playdef = \cs0playbuf;
		recdef  = \cs0recordbuf;
		bbcutbuf = bbcutbuf ?? { deallocate = true; BBCutBuffer.alloc(Server.default,44100,cutgroup.numChannels)};
		playdef.postln;
		recdef.postln;
	}
	free {
		if(deallocate,{ bbcutbuf.free });
	}

	renderBlock {| block, clock |
		var s, grpnum, dc, atk, rel, crv;

		s = cutgroup.server;
		grpnum = cutgroup.synthgroup.nodeID;
		//pbsfunc.tryPerform(\updateblock,  block);
		dutycycle.tryPerform(\updateblock,  block);
		atkprop.tryPerform(\updateblock,  block);
		relprop.tryPerform(\updateblock,  block);
		curve.tryPerform(\updateblock,  block);

		block.cuts.do({| cut, i |
			var dur;
			dc  = dutycycle.value(i,block);
			atk = atkprop.value(i,block);
			rel = relprop.value(i,block);
			crv = curve.value(i,block);
			dur = cut[1]*dc; //adjusted to

			if(i==0,{
				block.msgs[i].add([\s_new, recdef, s.nextNodeID, 1, grpnum, \bufnum, bbcutbuf.bufnum, \dur, dur, \inbus,inbus.value, \outbus, cutgroup.index, \atkprop, atk, \relprop, rel, \curve, crv]);
				},{ //this was missing dur*dc before, rationalised now
					block.msgs[i].add([\s_new, playdef, s.nextNodeID, 0,grpnum,\bufnum,bbcutbuf.bufnum,\dur,dur,\outbus,cutgroup.index]);
			});
			if(trace.notNil,{ trace.msg(block, i,0.0,0.0,\repeatlength,dur) });

		});
		//don't need to return block, updated by reference
	}

	*initClass {
		StartUp.add({
			SynthDef.writeOnce(\cs0playbuf,{| bufnum=0, outbus=0, dur=0.1 |
				var tmp;
				tmp = PlayBuf.ar(1, bufnum, 1, 1, 0, 1);
				Out.ar(outbus,tmp*EnvGen.kr(Env([1,1],[dur]),doneAction:2));

			});
			SynthDef.writeOnce(\cs0recordbuf,{ arg bufnum=0,inbus=8,outbus=0,dur=0.1,atkprop=0.0,relprop=0.0,curve=0;
				var in,tmp;
				in=In.ar(inbus, 2);
				in=in.sum;
				in=in*EnvGen.kr(Env([0,1,1,0],[atkprop,1.0-atkprop-relprop,relprop]*dur,curve),doneAction:2);
				tmp = RecordBuf.ar(in,bufnum,0,1,0,1,1,1);
				Out.ar(outbus,in);
			});
		});

	}

}