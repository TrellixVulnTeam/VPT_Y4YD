#include "AppObject.h"


void AppObject::PreInit(const char* img_path)
{
	image_path = img_path;
}

void AppObject::BasicInit(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	renderer_m = renderer;
	width = w;
	height = h;
	x_m = x;
	y_m = y;
	SDL_Surface* tmpsurface;
	tmpsurface = IMG_Load(image_path);
	texture = SDL_CreateTextureFromSurface(renderer_m, tmpsurface);
	SDL_FreeSurface(tmpsurface);
}

void AppObject::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	BasicInit(renderer, w, h, x, y);
}

void AppObject::draw()
{
	SDL_RenderCopy(renderer_m, texture, NULL, &destR);
}

void AppObject::update()
{
	destR.h = height;
	destR.w = width;
	destR.x = x_m;
	destR.y = y_m;
}

void AppObject::collide(int UpdateVal) {

}

void AppObject::input(SDL_Event e)
{
}

void AppObject::ChangeImage(const char* img_path)
{
	image_path = img_path;
	SDL_Surface* tmpsurface;
	tmpsurface = IMG_Load(image_path);
	texture = SDL_CreateTextureFromSurface(renderer_m, tmpsurface);
	SDL_FreeSurface(tmpsurface);
}

Text::Text(string font, string text, SDL_Color textcolor, int textsize)
{
	message = text;
	textsize_m = textsize;
	textcolor_m = textcolor;
	font_path = font;
	font_m = TTF_OpenFont(font.c_str(), textsize_m);
	tmpsurface = TTF_RenderText_Blended(font_m, text.c_str(), textcolor_m);
}

void Text::BasicInit(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	renderer_m = renderer;
	width = w;
	height = h;
	x_m = x;
	y_m = y;
	texture = SDL_CreateTextureFromSurface(renderer_m, tmpsurface);
	SDL_QueryTexture(texture, NULL, NULL, &width, &height);
}

void Text::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	BasicInit(renderer, w, h, x, y);
}

void Text::draw()
{
	SDL_RenderCopy(renderer_m, texture, NULL, &destR);
}

void Text::update()
{
	destR.h = height;
	destR.w = width;
	destR.x = x_m;
	destR.y = y_m;
}

void Text::GetTextSize()
{
	if(TTF_SizeText(font_m, message.c_str(), &text_w, &text_h) != -1){
		//cout << "Width : " << text_w << " Height: " << text_h << std::endl;
	}
}

void Text::ChangeText(string text)
{
	message = text;
	tmpsurface = TTF_RenderText_Blended(font_m, text.c_str(), textcolor_m);
	texture = SDL_CreateTextureFromSurface(renderer_m, tmpsurface);
	SDL_QueryTexture(texture, NULL, NULL, &width, &height);
}

Button::Button(string img_path, string hovered_img_path, Text* text, int text_w, int text_h, int x_offset, int y_offset)
{
	image_path1 = img_path;
	hovered_image_path = hovered_img_path;
	if (text == nullptr) {
		text_m = nullptr;
	}
	else {
		text_m = text;
		text_w_m = text_w;
		text_h_m = text_h;
		x_offset_m = x_offset;
		y_offset_m = y_offset;
	}
	isCollided = false;
}

void Button::collide(int CollisionVal)
{
	destR.h = height;
	destR.w = width;
	destR.x = x_m;
	destR.y = y_m;
	CollisionVal_m = CollisionVal;
	isCollided = CollisionVal != -1;
	if (text_m != nullptr) {
		text_m->update();
	}
}

void Button::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	//BasicInit(renderer, w, h, x, y);
	//same as BasicInit
	renderer_m = renderer;
	width = w;
	height = h;
	x_m = x;
	y_m = y;

	if (text_m != nullptr) {
		text_m->Init(renderer_m, text_w_m, text_h_m, x_m + x_offset_m, y_m + y_offset_m);
	}
	normalTexture = IMG_LoadTexture(renderer_m, image_path1.c_str());
	pressedTexture = IMG_LoadTexture(renderer_m, hovered_image_path.c_str());
}

void Button::draw()
{
	SDL_RenderCopy(renderer_m, isCollided ? pressedTexture : normalTexture, NULL, &destR);
	if (text_m != nullptr) {
		text_m->draw();
	}
}

void Button::input(SDL_Event e)
{
	if (e.type == SDL_MOUSEBUTTONUP && CollisionVal_m != -1) {
		cout << "test" << endl;
	}
}

TextField::TextField(string placeHolderText, string font_path, int textsize, int x_offset, int y_offset, char pwChar)
{
	pwChar_m = pwChar;
	placeHolderText_m = placeHolderText;
	font_path_m = font_path;
	textsize_m = textsize;
	x_offset_m = x_offset;
	y_offset_m = y_offset;
}

void TextField::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	PreInit("..\\..\\..\\textbox1.png");
	BasicInit(renderer, w, h, x, y);
	message = "";
	text_m = new Text(font_path_m, message, SDL_Color{0, 0, 0, 255}, textsize_m);
	text_m->Init(renderer_m, 0, 0, x_m + x_offset_m, y_m + y_offset_m);
	text_m->ChangeText(placeHolderText_m);
	hasclicked = false;
	hasclicked_prev = false;
}

void TextField::draw()
{
	SDL_RenderCopy(renderer_m, texture, NULL, &destR);
	text_m->draw();
}

void TextField::input(SDL_Event e)
{
	if (e.type == SDL_MOUSEBUTTONUP) {
		hasclicked_prev = hasclicked;
		if (CollisionVal_m != -1) {
			hasclicked = true;
		}
		else {
			hasclicked = false;
		}
		if (hasclicked != hasclicked_prev) {
			updateText();
		}
	}
	if (hasclicked == true) {
		if (e.type == SDL_TEXTINPUT && !hasTextReachedBorder()) {
			message = message + e.text.text;
			updateText();
		}
		if (e.type == SDL_KEYDOWN) {
			//message = message + SDL_GetKeyName(e.key.keysym.sym);
			if (e.key.keysym.sym == SDLK_BACKSPACE && message.size() != 0) {
				message.pop_back();
				updateText();
			}
		}
	}
}

void TextField::updateText() {
	text_m->ChangeText(message == "" && !hasclicked ? placeHolderText_m : pwChar_m == 0 ? message : string(message.length(), pwChar_m));
}

void TextField::update()
{
	destR.h = height;
	destR.w = width;
	destR.x = x_m;
	destR.y = y_m;
	text_m->GetTextSize();
	text_m->update();
}

void TextField::collide(int CollisionVal)
{
	CollisionVal_m = CollisionVal;
}

bool TextField::hasTextReachedBorder()
{	
	if (text_m->text_w + x_offset_m + 20 < width) {
		return false;
	}
	else {
		return true;
	}
}
