export class Category {
  id: string;
  name: string;
  icon: string;
  type: string;

  constructor(id: string, name: string, icon: string, type: string) {
    this.id = id;
    this.name = name;
    this.icon = icon;
    this.type = type;
  }
}
