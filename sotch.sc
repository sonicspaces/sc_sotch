
Sotch : Music {
	var <slider;

	setupServer {
		s = Server.default;
		options = ServerOptions()
		.sampleRate_(44100)
		.numInputBusChannels_(2)
		.numOutputBusChannels_(4)
		;
		s.options = options;
		s.volume.setVolumeRange(-90, 12);
	}
	allocBufs {
		bufs.add(Buffer.read(s, path ++ "sotch0.aif"));
		bufs.add(Buffer.read(s, path ++ "sotch1.aif"));
		bufs.add(Buffer.read(s, path ++ "sotch2.aif"));
		bufs.add(Buffer.read(s, path ++ "sotch3.aif"));
		bufs.add(Buffer.read(s, path ++ "logi0.aif"));
		bufs.add(Buffer.read(s, path ++ "ca0.wav"));
		bufs.add(Buffer.read(s, path ++ "ca1.wav"));
		bufs.add(Buffer.read(s, path ++ "ca2.wav"));
		bufs.add(Buffer.read(s, path ++ "ca3.wav"));
	}
	allocBusz {
		busz.add(Bus.audio(s, 1));
	}
	gui {
		//midi
		var lastTrig = Date.getDate.rawSeconds;
		var midiSpc = \midi.asSpec;
		var cmSpc = [0.1, 4.0].asSpec;
		//gui addition
		var h, w;
		//meters
		var iLevels, oLevels, levels, iFunc, oFunc, iSynth, oSynth, synthFunc;
		var numIns = s.options.numInputBusChannels;
		var numOuts = s.options.numOutputBusChannels;
		var separator;

		//gui
		if((GUI.id == 'qt').not, { GUI.qt });
		h = Window.screenBounds.height;
		w = Window.screenBounds.width;

		//window
		qwin = Window(this.class.asString, Rect(100, 100, w * 0.7, h * 0.5));
		qwin.alwaysOnTop = false;

		//midi
		MIDIClient.init;
		MIDIIn.connectAll;

		//meters
		separator = UserView();
		separator.drawFunc = {|v|
			Pen.strokeColor = Color.black;
			Pen.line(0@0, 0@v.bounds.height);
			Pen.stroke;
		};
		iLevels = Array.fill(numIns, {
			LevelIndicator()
			.drawsPeak_(true)
			.warning_(0.8)
			.numTicks_(9)
			.numMajorTicks_(3)
			;
		});
		oLevels = Array.fill(numOuts, {
			LevelIndicator()
			.drawsPeak_(true)
			.warning_(0.8)
			.numTicks_(9)
			.numMajorTicks_(3)
			;
		});
		levels = iLevels ++ [separator] ++ oLevels;

		iFunc = OSCFunc({| msg |
			{
				iLevels.do({|item, i|
					item.value     = msg[i * 2 + 4].ampdb.linlin(-40, 0, 0, 1);
					item.peakLevel = msg[i * 2 + 3].ampdb.linlin(-40, 0, 0, 1);
				});
			}.defer;
		}, '/i_reply', s.addr);
		iFunc.fix;

		oFunc = OSCFunc({| msg |
			{
				oLevels.do({|item, i|
					item.value     = msg[i * 2 + 4].ampdb.linlin(-40, 0, 0, 1);
					item.peakLevel = msg[i * 2 + 3].ampdb.linlin(-40, 0, 0, 1);
				});
			}.defer;
		}, '/o_reply', s.addr);
		oFunc.fix;

		synthFunc = {
			s.bind({
				iSynth = SynthDef('iLevels', {
					var sig;
					sig = In.ar(NumOutputBuses.ir, numIns);
					SendPeakRMS.kr(sig, 15, 1.5, '/i_reply');
				}).play(RootNode(s), nil, \addToHead);

				oSynth = SynthDef('oLevels', {
					var sig;
					sig = In.ar(0, numOuts);
					SendPeakRMS.kr(sig, 15, 1.5, '/o_reply');
				}).play(RootNode(s), nil, \addToTail);
			});
		};
		ServerTree.add(synthFunc, s);
		if(s.serverRunning, synthFunc);

		qwin.onClose = {
			iFunc.free;
			oFunc.free;
			iSynth.free;
			oSynth.free;
			ServerTree.remove(synthFunc, s);
		};

		//gui
		qwin.layout = HLayout(
			VLayout(
				StaticText()
				.string_("queue moves on the RELEASE of keys")
				,
				StaticText()
				.background_(Color.green(1, 0.2))
				.string_("forward: Space")
				,
				StaticText()
				.background_(Color.yellow(1, 0.2))
				.string_("rewind: Shift + Space")
				,
				StaticText()
				.background_(Color.blue(1, 0.2))
				.string_("reset: Shift + r")
				,
				StaticText()
				.string_("fired queue:")
				,
				tv1 = TextView()
				.editable_(false)
				.font_(qFont)
				.canFocus_(false)
				,
				StaticText()
				.string_("next queue:")
				,
				tv2 = TextView()
				.editable_(false)
				.font_(qFont)
				.canFocus_(false)
				,
				HLayout(
					StaticText().string_("jump to queue: "),
					NumberBox()
					.value_(0)
					.action_({|nb|
						this.jump2Q(nb.value);
					})
				)
			),
			[
				slider = Slider()
				.thumbSize_(40)
				.background_(Color.white)
				.canFocus_(false)
				.action_({|sl|
					if(~cm0.isNil.not && ~cm0.isPlaying, {
						~cm0.set(\real, cmSpc.map(sl.value));
					});
				})
				,
				stretch: 0.5]
			,
			[
				qText = StaticText()
				.font_(Font("Monaco", w * 0.15))
				.stringColor_(Color.black)
				.string_("Q")
				.align_('center')
				.minWidth_(w * 0.3)
				,
				stretch: 1
			],
			*levels
		);
		qwin.view.keyUpAction_({|v,c,m,u,k|
			if(u==32, {
				if(m.isShift, { this.rewindQ }, { this.forwardQ });
			});
			if(u==82, { this.resetQ });
		});
		this.resetQ;
		qwin.front;

		MIDIdef.cc(\forward, {| v |
			var time = Date.getDate.rawSeconds;
			if((v > 32) && ((time - lastTrig) > 0.5), {
				this.forwardQ;
				lastTrig = time;
			});
		}, 51, 1);//midi channel is 0-15 in sc

		MIDIdef.cc(\rewind, {| v |
			var time = Date.getDate.rawSeconds;
			if((v > 32) && ((time - lastTrig) > 0.5), {
				this.rewindQ;
				lastTrig = time;
			});
		}, 91, 1);

		MIDIdef.cc(\serverOnOff,{| v |
			var time = Date.getDate.rawSeconds;
			if((v > 32) && ((time - lastTrig) > 0.5), {
				if(s.serverRunning, { s.quit }, { s.boot });
				lastTrig = time;
			});
		}, 61, 1);

		MIDIdef.cc(\reset, {| v |
			var time = Date.getDate.rawSeconds;
			if((v > 32) && ((time - lastTrig) > 0.5), {
				this.resetQ;
				lastTrig = time;
			});
		}, 62, 1);

		MIDIdef.cc(\muteUnmute, {| v |
			var time = Date.getDate.rawSeconds;
			if((v > 32) && ((time - lastTrig) > 0.5), {
				if(s.volume.isMuted, { s.unmute }, { s.mute });
				lastTrig = time;
			});
		}, 12, 1);

		MIDIdef.program(\end, {| v |
			var time = Date.getDate.rawSeconds;
			if((v > 32) && ((time - lastTrig) > 0.5), {
				this.end;
				lastTrig = time;
			});
		});

		MIDIdef.cc(\mv2, {| v |
			{ slider.value = midiSpc.unmap(v) }.defer;
			if(~cm0.isNil.not && ~cm0.isPlaying && (v > 0), {
				~cm0.set(\real, cmSpc.map(midiSpc.unmap(v)));
			});
		}, 111, 1);

	}
	removeResponders {
		MIDIClient.disposeClient;
		MIDIdef.all.clear;
	}
	sendSynthDefs {

		SynthDef(\freeze0, {| gate=1, fade=1, amp=1, freq=1, wipe=1, width=1, pan=0.5 |
			var fft, akr, sig, env, buf;
			buf = LocalBuf(512).clear;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = Mix(SoundIn.ar([0, 1]).clip2(1)) * env;
			akr = Amplitude.kr(sig);
			fft = FFT(buf, sig);
			fft = PV_MagFreeze(fft, LFPulse.kr(freq, 0, 0.9));
			fft = PV_BinScramble(fft, wipe, width, akr > 0.2);
			sig = Pan2.ar(IFFT(fft) * env, LFNoise2.kr(freq, pan));
			Out.ar(2, sig);
		}).send(s);

		SynthDef(\freeze1, {| gate=1, fade=1, amp=1, freq=1, wipe=1, width=1, pan=0.5 |
			var fft, akr, sig, env;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = Mix(SoundIn.ar([0, 1]).clip2(1)) * env;
			akr = Amplitude.kr(sig);
			fft = FFT(LocalBuf(512).clear, sig);
			fft = PV_MagFreeze(fft, LFPulse.kr(TRand.kr(freq * 0.3, freq, akr > 0.2), 0, 0.9));
			fft = PV_BinScramble(fft, wipe, width, akr > 0.2);
			sig = Pan2.ar(IFFT(fft) * env, LFNoise2.kr(freq, pan));
			Out.ar(2, sig);
		}).send(s);

		SynthDef(\ca0, {| gate=1, fade=1, amp=1, buf, rate=1 |
			var sig, env;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = PlayBuf.ar(2, buf, rate, doneAction: 2) * env;
			Out.ar(0, sig);
		}).send(s);

		SynthDef(\ca1, {| gate=1, fade=1, amp=1, buf, rate=1 |
			var sig, env;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = PlayBuf.ar(2, buf, rate, doneAction: 2) * env;
			Out.ar(0, sig);
		}).send(s);

		SynthDef(\play, {| gate=1, fade=0, amp=0.1, buf=0, rate=1 |
			var sig, env;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = PlayBuf.ar(2, buf, rate, doneAction: 2) * env;
			Out.ar(0, sig);
		}).send(s);

		SynthDef(\lgst0, {| gate=1, fade=1, amp=0.1 |
			var lfo, pan, env, sig;
			pan = LFNoise2.kr(0.4);
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, 1, 0, 1, 2);
			sig = PlayBuf.ar(1, bufs[4], loop: 1);
			sig = sig * amp * env;
			sig = PanF2.ar(sig, pan);
			Out.ar(0, sig);
		}).send;

		SynthDef(\lgst1, {| gate=1, fade=1, amp=0.1 |
			var pan, env, sig;
			pan = LFNoise2.kr(0.5);
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, 1, 0, 1, 2);
			sig = PlayBuf.ar(1, bufs[4], 0.125, loop: 1).below2(0.125);
			sig = sig * amp * env;
			sig = PanF2.ar(sig, pan);
			Out.ar(0, sig);
		}).send;

		SynthDef(\cnfr0, {| gate=1, fade=1, amp=1, real=0.1 |
			var fft, sig, env;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = SoundIn.ar(1, env).clip2(1.0);
			fft = FFT(LocalBuf(2048).clear, sig);
			fft = PV_ConformalMap(fft, real, 1);
			sig = Pan2.ar(IFFT(fft), LFNoise2.kr(2, 0.4));
			Out.ar(2, sig);
		}).send;

		SynthDef(\cnfr1, {| gate=1, fade=1, amp=1, real=0.1 |
			var fft, sig, env;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = PlayBuf.ar(1, bufs[4], 0.125, loop: 1).below2(0.125);
			fft = FFT(LocalBuf(2048).clear, sig);
			fft = PV_ConformalMap(fft, real, 1);
			sig = IFFT(fft);
			sig = sig * env;
			sig = Pan2.ar(sig, LFNoise2.kr(2, 1));
			Out.ar(0, sig);
		}).send;

		SynthDef(\comb0, {| gate=1, fade=1, amp=1, freq=0.3 |
			var env, lfo, sig, akr;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, 1, 0, 1, 2);
			lfo = LFNoise2.kr(freq, 0.025, 0.035);
			sig = Mix(SoundIn.ar([0, 1], 2.0)).softclip;
			akr = Amplitude.kr(sig);
			sig = CombC.ar(sig, 0.06, lfo, 0.5, env);
			sig = FreqShift.ar(sig, akr * 30, [0, pi*0.5], add: sig.neg);
			Out.ar(2, sig * amp);
		}).send;

	}

}