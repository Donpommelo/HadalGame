#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 u_resolution;
uniform float u_time;

void main() {
	vec2 coord = gl_FragCoord.xy / u_resolution;
	float aTime = u_time * 0.25;

	float color = 0.0;

	color += tan(aTime * 0.5 + tan(coord.y * 5.0 + cos(aTime * 0.5 + coord.x * 30.0 + sin(coord.y * 30.0 + aTime * 2.0))) * 0.5);

	gl_FragColor = vec4(vec3(color - coord.x - cos(aTime * 0.5), 0.2, color + coord.y + sin(aTime * 0.5)), 1.0);
}
