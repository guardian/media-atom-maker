import React from 'react';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy
} from '@dnd-kit/sortable';
import {useSortable} from '@dnd-kit/sortable';
import {CSS} from '@dnd-kit/utilities';

export function SortableTag({id, removeFn, tag}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition
  } = useSortable({id});

  const style = {
    transform: CSS.Transform.toString(transform),
    transition
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <Tag tag={tag} removeFn={removeFn}></Tag>
    </div>
  );
}

const Tag = ({tag, removeFn}) =>
  <div
    key={`${tag.id}`}
    className="form__field__selected__tag form__field__selected__tag__draggable"
  >
    <span>
      {tag.detailedTitle}
    </span>
    <span
      data-no-dnd="true"
      className="form__field__tag__remove"
      onClick={() => removeFn(tag)}>
    </span>
  </div>;

const shouldHandleEvent = (element) =>{
  let cur = element;

  while (cur) {
    if (cur.dataset && cur.dataset.noDnd) {
      return false;
    }
    cur = cur.parentElement;
  }

  return true;
};

// Custom sensors because we don't want the drag to overrule the 'remove tag' button
// event listeners
export class CustomPointerSensor extends PointerSensor {
  static activators = [
    {
      eventName: 'onMouseDown',
      handler: ({ nativeEvent: event }) => {
        return shouldHandleEvent(event.target);
      }
    }
  ]
}

export class CustomKeyboardSensor extends KeyboardSensor {
  static activators = [
    {
      eventName: 'onKeyDown',
      handler: ({ nativeEvent: event }) => {
        return shouldHandleEvent(event.target);
      }
    }
  ]
}


export const DraggableTagList = ({tags, setTags, removeFn}) => {
  const sensors = useSensors(
    useSensor(CustomPointerSensor),
    useSensor(CustomKeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates
    })
  );

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragEnd={handleDragEnd}
    >
      <SortableContext
        items={tags}
        strategy={verticalListSortingStrategy}
      >
        {tags.map(tags => <SortableTag key={tags.id} id={tags.id} tag={tags} removeFn={removeFn} />)}
      </SortableContext>
    </DndContext>
  );

  function handleDragEnd(event) {
    const {active, over} = event;
    if (active.id !== over.id) {
        const oldIndex = tags.findIndex(tag => active.id === tag.id);
        const newIndex = tags.findIndex(tag => over.id === tag.id);;
        const newTags = arrayMove(tags, oldIndex, newIndex);
        setTags(newTags);
    }
  }
};
