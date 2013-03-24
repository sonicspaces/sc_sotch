
Sotch : Music {

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
	addResponders {
		var lastTrig = Date.getDate.rawSeconds;//.bootSeconds;
		var midiSpc = \midi.asSpec;
		var cmSpc = [0.1, 4.0].asSpec;
		var qwin, qnum, slider;
		var screen = Window.screenBounds;

		MIDIClient.init;
		MIDIIn.connectAll;

		qwin = Window(bounds: screen, border: false);
		qwin.alwaysOnTop_(true);
		qwin.alpha_(0.7);
		qnum = StaticText(qwin, qwin.view.bounds);
		qnum.font_(Font("Monaco", screen.height * 0.8));
		qnum.stringColor_(Color.black);
		qnum.string_("");
		qnum.align_('center');
		slider = EZSlider(qwin, 100@screen.height, "slider",
			'midi'.asSpec, nil, 0, false, 100, 100, 0, 50, 'vert', 0@0, 10@10);
		slider.font_(Font(size: 20));
		qwin.front;

		MIDIdef.cc(\q, {| v |
			var time = Date.getDate.rawSeconds;//.bootSeconds;
			if((v > 32) && ((time - lastTrig) > 0.5), {
				Routine({
					qnum.string_(que);
					this.forwardQ;
					qnum.stringColor_(Color.white);
					qnum.background_(Color.green);
					0.2.wait;
					qnum.stringColor_(Color.black);
					qnum.background_(Color.white);
				}).play(AppClock);
				lastTrig = time;
			});
		}, 51, 1);//midi channel is 0-15 in sc
		MIDIdef.cc(\mv2, {| v |
			{ slider.value = v }.defer;
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
			sig = Mix(SoundIn.ar([0, 1]).clip2(0.1)) * 10 * env;
			akr = Amplitude.kr(sig);
			fft = FFT(buf, sig);
			fft = PV_MagFreeze(fft, LFPulse.kr(freq, 0, 0.9));
			fft = PV_BinScramble(fft, wipe, width, akr > 0.2);
			sig = Pan2.ar(IFFT(fft) * env, LFNoise2.kr(freq, pan));
			Out.ar(0, sig);
		}).send(s);

		SynthDef(\freeze1, {| gate=1, fade=1, amp=1, freq=1, wipe=1, width=1, pan=0.5 |
			var fft, akr, sig, env;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = Mix(SoundIn.ar([0, 1]).clip2(0.1)) * 10 * env;
			akr = Amplitude.kr(sig);
			fft = FFT(LocalBuf(512).clear, sig);
			fft = PV_MagFreeze(fft, LFPulse.kr(TRand.kr(freq * 0.3, freq, akr > 0.2), 0, 0.9));
			fft = PV_BinScramble(fft, wipe, width, akr > 0.2);
			sig = Pan2.ar(IFFT(fft) * env, LFNoise2.kr(freq, pan));
			Out.ar(0, sig);
		}).send(s);

		SynthDef(\ca0, {
		| gate=1, fade=1, amp=1, buf, rate=1 |
			var sig, env;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = PlayBuf.ar(2, buf, rate, doneAction: 2) * env;
			Out.ar(0, sig);
		}).send(s);

		SynthDef(\ca1, {
		| gate=1, fade=1, amp=1, buf, rate=1 |
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
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = PlayBuf.ar(1, bufs[4], loop: 1);
			sig = sig * env;
			Out.ar(busz[0], sig);
			sig = PanF2.ar(sig, pan);
			Out.ar(0, sig);
		}).send(s);

		SynthDef(\lgst1, {| gate=1, fade=1, amp=0.1 |
			var pan, env, sig;
			pan = LFNoise2.kr(0.5);
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = PlayBuf.ar(1, bufs[4], 0.125, loop: 1).below2(0.125);
			sig = sig * env;
			Out.ar(busz[0], sig);
			sig = PanF2.ar(sig, pan);
			Out.ar(0, sig);
		}).send(s);

		SynthDef(\cnfr0, {| gate=1, fade=1, amp=1, real=0.1 |
			var fft, sig, env;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
			sig = SoundIn.ar(0, env, In.ar(busz[0])).clip2(1.0);
			fft = FFT(LocalBuf(2048).clear, sig);
			fft = PV_ConformalMap(fft, real, 1);
			sig = Pan2.ar(IFFT(fft), LFNoise2.kr(2, 0.4));
			Out.ar(0, sig);
		}).send(s);

		SynthDef(\comb0, {| gate=1, fade=1, amp=1, freq=0.3 |
			var env, lfo, sig, akr;
			env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, 1, 0, 1, 2);
			lfo = LFNoise2.kr(freq, 0.025, 0.035);
			sig = Mix(SoundIn.ar([0, 1], 2.0)).softclip;
			akr = Amplitude.kr(sig);
			sig = CombC.ar(sig, 0.06, lfo, 0.5, env);
			sig = FreqShift.ar(sig, akr * 30, [0, pi*0.5], add: sig.neg);
			Out.ar(0, sig * amp);
		}).send(s);

	}

}