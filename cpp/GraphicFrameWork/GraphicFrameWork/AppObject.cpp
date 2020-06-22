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
	texture = IMG_LoadTexture(renderer_m, image_path);
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
	texture = IMG_LoadTexture(renderer_m, image_path);
}

void AppObject::ApplyEffects() {
	if (tint.a != 0) {
		Utils::SDL_PushRendererState(renderer_m);
		Utils::SDL_SetRenderDrawSDLColor(renderer_m, tint);
		SDL_RenderFillRect(renderer_m, getBounds());
		Utils::SDL_PopRendererState(renderer_m);
	}
}

SDL_Rect* AppObject::getBounds() {
	return new SDL_Rect{ x_m, y_m, width, height };
}

Text::Text(string font, string text, SDL_Color textcolor, int textsize)
{
	message = text;
	textsize_m = textsize;
	textcolor_m = textcolor;
	font_path = font;
	font_m = TTF_OpenFont(font.c_str(), textsize_m);
	tmpsurface = TTF_RenderText_Blended(font_m, text.c_str(), textcolor_m);
	textds = 0;
	textde = -1;
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
	string displayMessage = message.substr(textds, text.length());
	if (textde != -1) {
		displayMessage = displayMessage.substr(0, ((size_t)textde)-textds);
	}
	tmpsurface = TTF_RenderText_Blended(font_m, displayMessage.c_str(), textcolor_m);
	texture = SDL_CreateTextureFromSurface(renderer_m, tmpsurface);
	SDL_QueryTexture(texture, NULL, NULL, &width, &height);
}

Button::Button(string img_path, string hovered_img_path, Text* text, int text_w, int text_h, int x_offset, int y_offset, void(*onclick)())
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
	onclick_m = onclick;
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
		onclick_m();
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
	cursorPos = 0;
	lastKeyPress = 0;
}

void TextField::Init(string projectDir, SDL_Renderer* renderer, int w, int h, int x, int y)
{
	string temp = projectDir + "..\\..\\textbox1.png";
	PreInit(temp.c_str());
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
	//Change every 600ms unless key press within 250ms
	if (hasclicked && (SDL_GetTicks() / 600) % 2 >= 1 || SDL_GetTicks() - lastKeyPress <= 250) {
		Utils::SDL_PushRendererState(renderer_m);
		SDL_SetRenderDrawColor(renderer_m, 0, 0, 0, 255);
		SDL_Rect textBounds = text_m->destR;
		TTF_SizeText(text_m->font_m, text_m->message.substr(text_m->textds, ((size_t)cursorPos)-text_m->textds).c_str(), &textBounds.w, &textBounds.h);
		SDL_RenderFillRect(renderer_m, new SDL_Rect{ textBounds.x + textBounds.w, textBounds.y, 2, textBounds.h });
		Utils::SDL_PopRendererState(renderer_m);
	}
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
		else if (hasclicked) {
			int x, y;
			SDL_GetMouseState(&x, &y);
			SDL_Rect* textBounds = text_m->getBounds();
			if (textBounds->x <= x) {
				if(x <= textBounds->x + textBounds->w) {
					x = x - textBounds->x;
					int searchPos = text_m->textds;
					int prevPxPos = 0;
					int ncursorPos = -1;
					while (searchPos <= text_m->textde) {
						int glyphsize;
						TTF_GlyphMetrics(text_m->font_m, text_m->message[searchPos], NULL, &glyphsize, NULL, NULL, NULL);
						int center = (glyphsize - prevPxPos) / 2;
						if (x >= prevPxPos && x < center) {
							ncursorPos = searchPos;
							break;
						}
						prevPxPos = prevPxPos + glyphsize;
						if (x >= center && x <= prevPxPos) {
							ncursorPos = searchPos+1;
							break;
						}
						searchPos++;
					}
					if (ncursorPos == -1) {
						ncursorPos = text_m->textde;
					}
					cursorPos = ncursorPos;
				} else {
					cursorPos = text_m->textde;
				}
			}
			else {
				cursorPos = text_m->textds;
			}
		}
	}
	if (hasclicked == true) {
		if (e.type == SDL_TEXTINPUT) {
			if (message == "") {
				text_m->textde = 0;
			}
			if (hasTextReachedBorder()) {
				text_m->textds++;
			}
			text_m->textde++;
			message = message.substr(0, cursorPos) + e.text.text + message.substr(cursorPos, message.length());
			cursorPos++;
			updateText();
		}
		if (e.type == SDL_KEYDOWN) {
			//message = message + SDL_GetKeyName(e.key.keysym.sym);
			SDL_Keycode kc = e.key.keysym.sym;
			if (kc == SDLK_BACKSPACE && cursorPos != 0) {
				message = message.substr(0, ((size_t)cursorPos)-1) + message.substr(cursorPos, message.length());
				text_m->textde--;
				if (cursorPos == text_m->textds || (text_m->textde == message.length() && text_m->textds != 0)) {
					text_m->textds--;
				}
				cursorPos--;
				updateText();
			}
			if (kc == SDLK_LEFT && cursorPos != 0) {
				if (cursorPos == text_m->textds) {
					text_m->textds--;
					text_m->textde--;
					updateText();
				}
				lastKeyPress = SDL_GetTicks();
				cursorPos--;
			}
			if (kc == SDLK_RIGHT && cursorPos != message.length()) {
				if (cursorPos == text_m->textde) {
					text_m->textds++;
					text_m->textde++;
					updateText();
				}
				lastKeyPress = SDL_GetTicks();
				cursorPos++;
			}
		}
	}
}

void TextField::updateText() {
	lastKeyPress = SDL_GetTicks();
	if (message == "") {
		text_m->textde = -1;
	}
	text_m->ChangeText(message == "" && !hasclicked ? placeHolderText_m : pwChar_m == 0 ? message : string(message.length(), pwChar_m));
	text_m->GetTextSize();
	text_m->update();
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
	return text_m->text_w + x_offset_m + 20 >= width;
}

Overlay::Overlay(string font, string text, SDL_Color bacgroundColor, SDL_Color textColor, int textsize, int x_offset, int y_offset, Uint32 displayTime, vector<AppObject*>* overlays) {
	text_m = new Text(font, text, textColor, textsize);
	bacgroundColor_m = bacgroundColor;
	x_offset_m = x_offset;
	y_offset_m = y_offset;
	displayTime_m = displayTime;
	hasStart = false;
	overlays_m = overlays;
	id = -1;
	bounds = nullptr;
	startTime = 0;
}

void Overlay::Init(SDL_Renderer* renderer, int w, int h, int x, int y) {
	renderer_m = renderer;
	width = w;
	height = h;
	x_m = x;
	y_m = y;
	text_m->Init(renderer, w, h, x + x_offset_m, y + y_offset_m);
	bounds = new SDL_Rect{ x, y, w, h };
}

void Overlay::Init(SDL_Renderer* renderer, int x, int y) {
	renderer_m = renderer;
	x_m = x;
	y_m = y;
	text_m->Init(renderer, 0, 0, x + x_offset_m, y + y_offset_m);
	width = text_m->width+x_offset_m+x_offset_m;
	height = text_m->height + y_offset_m + y_offset_m;;
	bounds = getBounds();
}

void Overlay::draw() {
	if (!hasStart) {
		hasStart = true;
		startTime = SDL_GetTicks();
	}
	Uint8 r, g, b, a;
	SDL_GetRenderDrawColor(renderer_m, &r, &g, &b, &a);
	SDL_SetRenderDrawColor(renderer_m, bacgroundColor_m.r, bacgroundColor_m.g, bacgroundColor_m.b, bacgroundColor_m.a);
	SDL_RenderFillRect(renderer_m, bounds);
	SDL_SetRenderDrawColor(renderer_m, r, g, b, a);
	text_m->draw();
}

void Overlay::update() {
	if (hasStart) {
		if (SDL_GetTicks() - startTime > displayTime_m) {
			if (id != -1) {
				(*overlays_m)[id] = nullptr;
			}
		}
		text_m->update();
	}
}

CheckBox::CheckBox(string font, int textsize, int x_offset, int y_offset, string backround_img)
{
	textsize_m = textsize;
	x_offset_m = x_offset;
	y_offset_m = y_offset;
	backround_img_m = backround_img;
}

void CheckBox::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	PreInit(backround_img_m.c_str());
	BasicInit(renderer, w, h, x, y);
}

void CheckBox::draw()
{
	SDL_RenderCopy(renderer_m, texture, NULL, &destR);
	for (unsigned int i = 0; i < checkboxes.size(); i++) {
		checkboxes[i]->draw();
	}
}

void CheckBox::AddCheckBox(string text)
{
	//checkboxes.push_back(new Button("", "", new Text(font_m, text, SDL_Color{0,0,0,255}, textsize_m)));
	if (checkboxes.size() - 1 == 0) {
		checkboxes[checkboxes.size() - 1]->Init(renderer_m, 100, 100, x_m, y_m);
	}
	else {
		checkboxes[checkboxes.size() - 1]->Init(renderer_m, 100, 100, x_m, (checkboxes.size() - 1) * 100);
	}
}

void CheckBox::input(SDL_Event e)
{
	for (unsigned int i = 0; i < checkboxes.size(); i++) {
		checkboxes[i]->input(e);
	}
}

void CheckBox::update()
{
	destR.h = height;
	destR.w = width;
	destR.x = x_m;
	destR.y = y_m;
	for (unsigned int i = 0; i < checkboxes.size(); i++) {
		checkboxes[i]->update();
	}
}


void CheckBox::collide(int CollisionVal)
{
	for (unsigned int i = 0; i < checkboxes.size(); i++) {
		checkboxes[i]->collide(CollisionVal);
	}
}

TextBox::TextBox(string font_path, int textsize, int x_offset, int y_offset, string text)
{
	font_path_m = font_path;
	textsize_m = textsize;
	message = text;
	x_offset_m = x_offset;
	y_offset_m = y_offset;
}


void TextBox::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	PreInit("C:\\Users\\richa\\source\\repos\\VPT\\cpp\\GraphicFrameWork\\GraphicFrameWork\\textbox1.png");
	BasicInit(renderer, w, h, x, y);
	text_m = new Text(font_path_m, message, SDL_Color{ 0, 0, 0, 255 }, textsize_m);
	text_m->Init(renderer_m, 0, 0, x_m + x_offset_m, y_m + y_offset_m);
}

void TextBox::draw()
{
	SDL_RenderCopy(renderer_m, texture, NULL, &destR);
	text_m->draw();
}

void TextBox::update()
{
	destR.h = height;
	destR.w = width;
	destR.x = x_m;
	destR.y = y_m;
	text_m->GetTextSize();
	text_m->update();
}

SimpleButton::SimpleButton(Text* text, int x_offset, int y_offset, void(*onclick)(), Background* background, Border* border, SDL_Color hoverTint, SDL_Color clickTint)
{
	background_m = background;
	border_m = border;
	hoverTint_m = hoverTint;
	clickTint_m = clickTint;
	if (text == nullptr) {
		text_m = nullptr;
	}
	else {
		text_m = text;
		x_offset_m = x_offset;
		y_offset_m = y_offset;
	}
	isCollided = false;
	isMouseDown = false;
	onclick_m = onclick;
}

void SimpleButton::collide(int CollisionVal)
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

void SimpleButton::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	//BasicInit(renderer, w, h, x, y);
	//same as BasicInit
	renderer_m = renderer;
	width = w;
	height = h;
	x_m = x;
	y_m = y;
	if (text_m != nullptr) {
		text_m->Init(renderer_m, 0, 0, x_m + x_offset_m, y_m + y_offset_m);
		text_m->update();
		text_w_m = text_m->width;
		text_h_m = text_m->height;
	}
}

void SimpleButton::Init(SDL_Renderer* renderer, int x, int y)
{
	renderer_m = renderer;
	x_m = x;
	y_m = y;
	if (text_m != nullptr) {
		text_m->Init(renderer_m, 0, 0, x_m + x_offset_m, y_m + y_offset_m);
		text_m->update();
		text_w_m = text_m->width;
		text_h_m = text_m->height;
	}
	width = text_w_m + x_offset_m + x_offset_m;
	height = text_h_m + y_offset_m + y_offset_m;
}

void SimpleButton::draw()
{
	if (isCollided) {
		if (isMouseDown) {
			tint = clickTint_m;
		}
		else {
			tint = hoverTint_m;
		}
	}
	else {
		tint = SDL_Color{ 0, 0, 0, 0 };
	}
	const SDL_Rect* bounds = getBounds();
	background_m->draw(renderer_m, bounds);
	border_m->draw(renderer_m, bounds);
	if (text_m != nullptr) {
		text_m->draw();
	}
	ApplyEffects();
}

void SimpleButton::input(SDL_Event e)
{
	if (e.type == SDL_MOUSEBUTTONDOWN) {
		isMouseDown = true;
	}
	if (e.type == SDL_MOUSEBUTTONUP) {
		isMouseDown = false;
		if (isCollided) {
			onclick_m();
		}
	}
}