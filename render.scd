
s.boot;
//logi0.aif (after editing in audacity)
(
SynthDef(\render, {| amp=0.1 |
	var sig;
	sig = Logist0.ar(22050, 1.5);
	sig = LeakDC.ar(sig);
	sig = sig * amp;
	Out.ar(0, sig.dup);
}).load;
)
Synth(\render);
Synth.render(\render, [amp: 1], 1, 5);

b=Buffer.read(s, "/Users/yota/Library/Application Support/SuperCollider/Extensions/sc_sotch/logi0.aif");
{ PlayBuf.ar(1, b, loop: 1) * 0.1.dup }.play;
{ PlayBuf.ar(1, b, 0.25, loop: 1).below2(0.125) }.play;
b.free;
{ SinOsc.ar.below2(1) }.scope;

(
SynthDef(\ca0, {
| gate=1, fade=1, amp=1, smprt=22050, smpwd=16, seed=128, seedlag=1, xfrq=0.1 |
	var sig, env;
	env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
	sig = CA1.ar(smprt, smpwd, [122, 126], seed.ramp(seedlag), Dust.kr(xfrq), env);
	Out.ar(0, sig);
}).send(s);

SynthDef(\ca1, {
| gate=1, fade=1, amp=1, smprt=22050, smpwdfq=0.1, smpwd=16, seed=128, seedlag=1, xfrq=0.1 |
	var sig, env;
	env = EnvGen.kr(Env.asr(fade, 1, fade, 3), gate, amp, 0, 1, 2);
	sig = CA1.ar(smprt, LFNoise2.kr(smpwdfq).range(16, 60), [122, 126], 
			seed.ramp(seedlag), Dust.kr(xfrq), env);
	Out.ar(0, sig);
}).send(s);
)
//ca0.wav		
s.rek(2);
~ca = Synth(\ca0, [fade: 3, amp: 0.2, smprt: 10000, smpwd: 30, xfrq: 200, seed: 200]);
s.stoprek;

//ca1.wav
s.rek(2);
~ca = Synth(\ca0, [fade: 0.01, amp: 0.2, smprt: 44100, smpwd: 30, xfrq: 200, seed: 200]);
~ca.set(\smpwd, 60);
s.stoprek

//11
s.rek(2);
~ca = Synth(\ca1, [fade: 0.07, amp: 0.1, smprt: 22050, smpwdfq: 1.3, xfrq: 50, seed: 200]);

//13
s.rek(2);
~ca = Synth(\ca1, [fade: 0.07, amp: 0.1, smprt: 22050, smpwdfq: 2.5, xfrq: 50, seed: 200]);
s.stoprek
