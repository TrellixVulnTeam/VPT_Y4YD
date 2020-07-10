#include "PlaceableObjects.h"

string PlaceableBounding::FormatReleventData(int numArgs, ...) {
	string out;
	va_list vArgs;
	va_start(vArgs, numArgs);
	for (int i = 0; i < numArgs; i++) {
		string arg = va_arg(vArgs, string);
		arg = regex_replace(arg, regex("\\\\"), "\\\\");
		arg = regex_replace(arg, regex("\""), "\\\"");
		out += "\"";
		out += arg;
		out += "\",";
	}
	va_end(vArgs);
	return out.substr(0, out.length() - 1);
}

PlaceableText::PlaceableText() {
	text = new Text(fontPath, "Text", SDL_Color{ 0, 0, 0, 255 }, 100);
}

void PlaceableText::Init(SDL_Renderer* renderer, int w, int h, int x, int y) {
	BasicInit(renderer, w, h, x, y);
	text->Init(renderer, w, h, x, y);
}

void PlaceableText::draw() {
	text->draw();
}

void PlaceableText::update() {
	text->update();
}