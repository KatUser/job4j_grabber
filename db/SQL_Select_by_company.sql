-- 1. В одном запросе получить
-- имена всех person, которые не состоят в компании с id = 5;
-- название компании для каждого человека.

select p.name as "Имя человека",
c.name as "Название компании"
    from person as p
    inner join company as c on
    p.company_id = c.id
    where c.id < 5;


-- 2. Необходимо выбрать название компании с максимальным количеством человек
-- + количество человек в этой компании.
-- Нужно учесть, что таких компаний может быть несколько.
select  c.name as "Название компании",
count(p.name) as "Кол-во человек"
    from company c inner join person p
    on p.company_id = c.id
        group by c.name, p.company_id
        having count(p.company_id) in (select  max(n)
            from (select p.company_id, count (p.company_id) as n
                from person p group by p.company_id));