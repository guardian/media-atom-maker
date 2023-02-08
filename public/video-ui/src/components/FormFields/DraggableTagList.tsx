import React from 'react';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  useSensor,
  useSensors,
  MouseSensor,
  DragEndEvent
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy
} from '@dnd-kit/sortable';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

type Tag = {
  id: string;
  detailedTitle: string;
  webTitle: string;
};

const SortableTag = ({
  tag,
  removeFn
}: {
  tag: Tag;
  removeFn: (t: Tag) => void;
}) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition
  } = useSortable({ id: tag.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <TagElement tag={tag} removeFn={removeFn}></TagElement>
    </div>
  );
};

const TagElement = ({
  tag,
  removeFn
}: {
  tag: Tag;
  removeFn: (tag: Tag) => void;
}) => (
  <div
    key={`${tag.id}`}
    className="form__field__selected__tag form__field__selected__tag__draggable"
  >
    <span>{tag.detailedTitle}</span>
    <span
      tabIndex={0}
      role="button"
      data-no-dnd="true"
      className="form__field__tag__remove"
      onClick={() => removeFn(tag)}
      onKeyDown={(e) => {
        if (e.key === "Enter"){
          removeFn(tag);
        }
      }}
    ></span>
  </div>
);

const shouldHandleEvent = (element: HTMLElement) => {
  let cur = element;

  while (cur) {
    if (cur.dataset && cur.dataset.noDnd) {
      return false;
    }
    cur = cur.parentElement;
  }

  return true;
};

// We use a custom mouse sensor because we don't want the drag listener to
// overrule the 'remove tag' button event listener
class CustomMouseSensor extends MouseSensor {
  static activators = [
    {
      eventName: 'onMouseDown' as const,
      handler: ({ nativeEvent: event }: React.MouseEvent) => {
        return shouldHandleEvent(event.target as HTMLElement);
      }
    }
  ];
}

export const DraggableTagList = ({
  tags,
  setTags,
  removeFn
}: {
  tags: Tag[];
  setTags: (tags: Tag[]) => void;
  removeFn: (t: Tag) => void;
}) => {
  const sensors = useSensors(
    useSensor(CustomMouseSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates
    })
  );

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (active.id !== over.id) {
      const oldIndex = tags.findIndex(tag => active.id === tag.id);
      const newIndex = tags.findIndex(tag => over.id === tag.id);
      const newTags = arrayMove(tags, oldIndex, newIndex);
      setTags(newTags);
    }
  };

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragEnd={handleDragEnd}
    >
      <SortableContext items={tags} strategy={verticalListSortingStrategy}>
        {tags.map(tags => (
          <SortableTag key={tags.id} tag={tags} removeFn={removeFn} />
        ))}
      </SortableContext>
    </DndContext>
  );
};
