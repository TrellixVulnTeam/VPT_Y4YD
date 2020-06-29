#include "Component.h"

ComponentManager::ComponentManager()
{
}

void ComponentManager::AttachComponent(Component* component, AppObject* parent)
{
	if (component->RunSector() == "update") {
		UpdateSectorComponents.push_back(component);
		UpdateSectorComponents[UpdateSectorComponents.size() - 1]->Init(parent);
	}
	if (component->RunSector() == "input") {
		InputSectorComponents.push_back(component);
		InputSectorComponents[InputSectorComponents.size() - 1]->Init(parent);
	}
	if (component->RunSector() == "draw") {
		DrawSectorComponents.push_back(component);
		DrawSectorComponents[DrawSectorComponents.size() - 1]->Init(parent);
	}
}


void Component::BasicInit(AppObject *parent)
{
	parent_m = parent;
}

void Component::Init(AppObject *parent)
{
	BasicInit(parent);
}

int Component::run(vector <AppObject*> AppObjects)
{
	return 0;
}

int Component::id()
{
	return 0;
}

string Component::RunSector()
{
	return string();
}

int TestComponent::run(vector <AppObject*> AppObjects)
{
	cout << "test" << endl;
	return 0;
}

int TestComponent::id()
{
	return 1;
}

string TestComponent::RunSector()
{
	return "update";
}

CollisionBox::CollisionBox(vector<AppObject*> AppObjects)
{
	AppObjects_m = AppObjects;
}

int CollisionBox::run(vector <AppObject*> AppObjects)
{
	AppObjects_m = AppObjects;
	if (colNot0) {
		for (unsigned int i = 0; i < AppObjects_m.size(); i++) {
			if (isCollided(AppObjects_m[i]) && AppObjects_m[i]->id != parent_m->id) {
				return i;
			}
		}
	}
	else if(AppObjects_m.size() > 0 && isCollided(AppObjects_m[0])) {
		return 0;
	}
	return -1;
}

int CollisionBox::id()
{
	return 2;
}

bool CollisionBox::isCollided(AppObject* object)
{
	int ax = parent_m->x_m;
	int bx = object->x_m;
	int ay = parent_m->y_m;
	int by = object->y_m;
	int aw = parent_m->width;
	int bw = object->width;
	int ah = parent_m->height;
	int bh = object->height;
	/*if (ax > bx && ax < bx + bw || ax + aw > bx && ax + aw < bx + bw) {
		if (ay > by && ay < by + bh || ay + ah  > by && ay + ah < by + bh) {
			return true;
		}
	}*/

	if (ax < bx + bw &&
		ax + aw > bx &&
		ay < by + bh &&
		ay + ah > by) {
		return true;
	}

	/*if (
	ax + aw >= bx &&
    bx + bw >= ax &&
	ay + ah >= by &&
	by + bh >= ay
	) 
	{
		return true;
	}*/

	return false;
}

string CollisionBox::RunSector()
{
	return "update";
}

CollisionCircle::CollisionCircle(vector<AppObject*> AppObjects)
{
	AppObjects_m = AppObjects;
}

int CollisionCircle::run(vector <AppObject*> AppObjects)
{
	AppObjects_m = AppObjects;
	for (unsigned int i = 0; i < AppObjects_m.size(); i++) {
		if (isCollided(AppObjects_m[i])) {
			return i;
		}
	}
	return -1;
}

int CollisionCircle::id()
{
	return 3;
}

bool CollisionCircle::isCollided(AppObject* object)
{
	int ax = parent_m->x_m;
	int bx = object->x_m;
	int ay = parent_m->y_m;
	int by = object->y_m;
	double aw = parent_m->width;
	double bw = object->width;
	int ah = parent_m->height;
	int bh = object->height;


	int distx = ax - bx;
	int disty = ay - by;

	double dist = sqrt(distx * distx + disty * disty);

	if (dist < aw + bw){
		return true;
	}
	return false;
}

string CollisionCircle::RunSector()
{
	return "update";
}
